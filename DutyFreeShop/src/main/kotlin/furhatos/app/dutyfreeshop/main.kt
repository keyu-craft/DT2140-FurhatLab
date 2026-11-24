package furhatos.app.dutyfreeshop

import furhatos.app.dutyfreeshop.flow.*
import furhatos.skills.Skill
import kotlinx.coroutines.runBlocking

class DutyfreeshopSkill : Skill() {
    override fun start() {
        runBlocking {
            Init.run()
        }
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
