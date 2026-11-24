package furhatos.app.hotelreceptionist

import furhatos.app.hotelreceptionist.flow.*
import furhatos.skills.Skill
import kotlinx.coroutines.runBlocking

class HotelreceptionistSkill : Skill() {
    override fun start() {
        runBlocking {
            Init.run()
        }
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
