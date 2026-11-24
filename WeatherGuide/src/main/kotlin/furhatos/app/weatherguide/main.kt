package furhatos.app.weatherguide

import furhatos.app.weatherguide.flow.*
import furhatos.skills.Skill
import kotlinx.coroutines.runBlocking

class WeatherguideSkill : Skill() {
    override fun start() {
        runBlocking {
            Init.run()
        }
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
