package furhatos.app.hotelreceptionist.flow

import furhatos.flow.*
import furhatos.llm.*
import furhatos.llm.tools.*

val receptionistGenerator = object: ResponseGenerator() {
    override val agentName = "Receptionist"
    override val systemPrompt = """
        You are a polite hotel receptionist.
        When a guest asks about their reservation, use the FindReservation tool to look it up.
        If no reservation is found, apologize politely and explain you cannot find it in the system.
        If the guest complains and asks to speak to the manager, use the TalkToManager tool.
        Keep your responses concise and suitable for spoken conversation.
    """.trimIndent()
    override val LLM = OpenAI(model = "gpt-6-luna", defaultParams = LLMRequestParams(reasoningEffort = ReasoningEffort.NONE))
}
val managerGenerator = object: ResponseGenerator() {
    override val agentName = "Manager"
    override val systemPrompt = """
        You are the hotel manager.
        Apologize sincerely for the reservation issue and help the guest find a new available room (for example, offer a standard double room or a deluxe suite).
        Keep your responses helpful, calm, and concise for spoken conversation.
    """.trimIndent()
    override val LLM = OpenAI(model = "gpt-6-luna", defaultParams = LLMRequestParams(reasoningEffort = ReasoningEffort.NONE))
}

data class ReservationQuery(val name: String)

val findReservation = functionTool<ReservationQuery>("FindReservation") {
    description = "Look up a hotel reservation by the guest's name"
    function { query ->
        println("Looking up reservation for: ${query.name}")
        "No reservation found for ${query.name}."
    }
}

val talkToManager = tool(
    name = "TalkToManager",
    description = "Switch to the hotel manager when the guest asks to talk to the manager"
)

val endConversation = tool(
    name = "EndConversation",
    description = "End the conversation if the user says goodbye"
)

val Receptionist: State = state {
    onEntry {
        furhat.setVoiceByName("Danielle")
        furhat.setFace(mask = "adult", character = "Marty")
        furhat.attendClosestUser()
        furhat.startListening()
        furhat.say(receptionistGenerator.generate("Something like: Welcome to our hotel! How may I help you today?"))
    }
    addFunctionTools(findReservation)
    addTool(talkToManager) {
        furhat.say(receptionistGenerator.generate("Something like: I'm very sorry about that. Let me get the manager for you right away.")).onEnd {
            goto(Manager)
        }
    }
    onUserTurnEnd {
        receptionistGenerator.generateWithTools(this) {
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
val Manager: State = state {
    onEntry {
        furhat.setVoiceByName("Matthew")
        furhat.setFace(mask = "adult", character = "Alex")
        furhat.startListening()
        furhat.say(managerGenerator.generate("Something like: Hello, I am the hotel manager. I apologize for the trouble with your reservation. Let's find you a new room."))
    }
    addTool(endConversation) {
        furhat.say(managerGenerator.generate("Something like: Everything is set for your new room. Enjoy your stay, goodbye!")).onEnd {
            furhat.users.disengage()
            goto(Idle)
        }
    }
    onUserTurnEnd {
        managerGenerator.generateWithTools(this) {
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
