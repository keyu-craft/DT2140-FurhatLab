package furhatos.app.dutyfreeshop.flow

import furhatos.flow.*
import furhatos.llm.*
import furhatos.llm.tools.*
import java.net.URI

val newEmployeeGenerator = object: ResponseGenerator() {
    override val agentName = "NewEmployee"
    override val systemPrompt = """
        You are a polite but inexperienced new cashier at a Swedish airport duty-free shop.
        Prices are in Swedish Krona (SEK), and the customer is paying with a US Dollar (USD) credit card.
        You can use the CheckExchangeRate tool to check the real SEK to USD rate if the customer asks about the price.
        However, the POS terminal defaults to charging in USD with a poor conversion rate, and you do NOT know how to switch the POS terminal back to SEK.
        When the customer asks to pay in SEK instead of USD on the POS machine, admit you are new and don't know how to change the POS setting, and call the CallSeniorEmployee tool to get help from your senior colleague.
        Keep your responses concise and natural for spoken conversation.
    """.trimIndent()
    override val LLM = OpenAI(model = "gpt-6-luna", defaultParams = LLMRequestParams(reasoningEffort = ReasoningEffort.NONE))
}
val seniorEmployeeGenerator = object: ResponseGenerator() {
    override val agentName = "SeniorEmployee"
    override val systemPrompt = """
        You are an experienced senior cashier at a Swedish airport duty-free shop.
        Your new colleague called you over because the customer wants to pay in SEK instead of the POS terminal's USD conversion (DCC).
        Reassure the customer, explain that you can easily select "Pay in SEK (Local Currency)" on the POS terminal so their own bank handles the conversion at a better rate.
        You can also use the CheckExchangeRate tool to tell them the real mid-market USD equivalent if helpful.
        Complete the checkout politely and keep your responses concise for spoken conversation.
    """.trimIndent()
    override val LLM = OpenAI(model = "gpt-6-luna", defaultParams = LLMRequestParams(reasoningEffort = ReasoningEffort.NONE))
}

data class CurrencyQuery(val amount: Double, val from: String, val to: String)
fun fetchExchangeRate(query: CurrencyQuery): String {
    val result = URI.create("https://api.frankfurter.app/latest?amount=${query.amount}&from=${query.from}&to=${query.to}").toURL().readText()
    println("Frankfurter API Result: $result")
    return result
}

val checkExchangeRate = functionTool<CurrencyQuery>("CheckExchangeRate") {
    description = "Check the live currency exchange rate and converted amount between two currencies (e.g. from SEK to USD) using the Frankfurter API"
    function { query ->
        println("Checking exchange rate: $query")
        fetchExchangeRate(query)
    }
}

val callSeniorEmployee = tool(
    name = "CallSeniorEmployee",
    description = "Call the senior employee when the new employee does not know how to switch the POS terminal from USD to SEK"
)

val endConversation = tool(
    name = "EndConversation",
    description = "End the conversation when the checkout is finished or the user says goodbye"
)

val NewEmployee: State = state {
    onEntry {
        furhat.setVoiceByName("Danielle")
        furhat.setFace(mask = "adult", character = "Marty")
        furhat.attendClosestUser()
        furhat.startListening()
        furhat.say(newEmployeeGenerator.generate("Something like: Hi, welcome to our duty-free shop! Your total is 1,000 SEK. How would you like to pay?"))
    }
    addFunctionTools(checkExchangeRate)
    addTool(callSeniorEmployee) {
        furhat.say(newEmployeeGenerator.generate("Something like: Sorry, I'm new here and I'm not sure how to switch the POS terminal to SEK. Let me ask my senior colleague to help us!")).onEnd {
            goto(SeniorEmployee)
        }
    }
    onUserTurnEnd {
        newEmployeeGenerator.generateWithTools(this) {
            furhat.say(it)
        }
    }
    onUserLeave {
        furhat.dialogHistory.clear()
        goto(Idle)
    }
    onExit {
        furhat.stopListening()
    }
}
val SeniorEmployee: State = state {
    onEntry {
        furhat.setVoiceByName("Matthew")
        furhat.setFace(mask = "adult", character = "Alex")
        furhat.startListening()
        furhat.say(seniorEmployeeGenerator.generate("Something like: Hi there, I can help with that! No problem, we can select SEK directly on the POS terminal for you."))
    }
    addFunctionTools(checkExchangeRate)
    addTool(endConversation) {
        furhat.say(seniorEmployeeGenerator.generate("Something like: You're all set, paid in SEK! Have a great flight, goodbye!")).onEnd {
            furhat.users.disengage()
            goto(Idle)
        }
    }
    onUserTurnEnd {
        seniorEmployeeGenerator.generateWithTools(this) {
            furhat.say(it)
        }
    }
    onUserLeave {
        goto(Idle)
    }
    onExit {
        furhat.stopListening()
        furhat.dialogHistory.clear()
    }
}
