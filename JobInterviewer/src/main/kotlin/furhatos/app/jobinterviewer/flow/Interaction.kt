package furhatos.app.jobinterviewer.flow

import furhatos.flow.*
import furhatos.llm.*
import furhatos.llm.tools.*

val generator = object: ResponseGenerator() {

    override val agentName = "Furhat"

    override val systemPrompt = """
        You are a friendly and professional job interviewer conducting a short interview for a software/AI engineering position.
        Go through this sequence of questions, asking ONE question at a time:
        1. Ask the candidate to briefly introduce themselves and their background.
        2. Ask about a challenging project they worked on and how they solved problems in it.
        3. Ask about their biggest strength and how they work in a team.
        4. Ask why they are interested in this role and if they have any questions.
        Give brief, natural acknowledgments to their answers before moving to the next question.
        Keep your responses concise and suitable for spoken conversation.
    """.trimIndent()

    override val LLM = OpenAI(model = "gpt-6-luna", defaultParams = LLMRequestParams(reasoningEffort = ReasoningEffort.NONE))

}

val endConversation = tool(
    name = "EndConversation",
    description = "End the conversation if the user indicates that they want to end the conversation, for example by saying goodbye"
)

val Interaction: State = state {

    onEntry {
        furhat.attendClosestUser()
        furhat.startListening()
        furhat.interruptable = true // 如果没戴耳机出现自己打断自己，把这行删掉或改成 false
        furhat.say(generator.generate("Something like: Hi there, welcome to this job interview!"))
    }

    addTool(endConversation) {
        furhat.say(generator.generate("Something like: Goodbye, it was nice talking to you")).onEnd {
            furhat.users.disengage()
            goto(Idle)
        }
    }

    onUserTurnEnd {
        generator.generateWithTools(this) {
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