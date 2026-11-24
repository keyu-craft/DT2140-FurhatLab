package furhatos.app.weatherguide.flow

import furhatos.flow.*
import furhatos.llm.*
import java.net.URI
import furhatos.llm.tools.*

val generator = object: ResponseGenerator() {

    override val agentName = "Furhat"

    override val systemPrompt = """
    You are a helpful and friendly weather guide robot.
    Use the provided weather tools to look up current weather and forecasts for any location the user asks about.
    Summarize the weather naturally and concisely for spoken conversation (do not read raw JSON or coordinates aloud).
    """.trimIndent()

    override val LLM = OpenAI(model = "gpt-6-luna", defaultParams = LLMRequestParams(reasoningEffort = ReasoningEffort.NONE))

}
data class GeoLocation(val latitude: Double, val longitude: Double)
fun checkWeather(location: GeoLocation, param: String): String {
    val result = URI.create("https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&$param").toURL().readText()
    println("Result: $result")
    return result
}
val askWeatherToday = functionTool<GeoLocation>("AskWeatherToday") {
    description = "Ask about today's weather for a given location"
    function { location ->
        println("Checking today's weather for $location")
        checkWeather(location, "current=temperature_2m,wind_speed_10m,rain,showers")
    }
}

val askWeatherForecast = functionTool<GeoLocation>("AskWeatherForecast") {
    description = "Ask about a forecast for a given location"
    function { location ->
        println("Checking the forecast for $location")
        checkWeather(location, "daily=temperature_2m_max,rain_sum")
    }
}

val endConversation = tool(
    name = "EndConversation",
    description = "End the conversation if the user indicates that they want to end the conversation, for example by saying goodbye"
)

val Interaction: State = state {

    onEntry {
        furhat.attendClosestUser()
        furhat.startListening()
        furhat.say(generator.generate("Something like: Hi there! I'm your weather guide. Which city's weather would you like to know about?"))
    }

    addFunctionTools(askWeatherToday, askWeatherForecast)

    addTool(endConversation) {
        furhat.say(generator.generate("Something like: Goodbye, have a great day!")).onEnd {
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
