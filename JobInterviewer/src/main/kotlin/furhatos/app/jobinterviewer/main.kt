package furhatos.app.jobinterviewer

import furhatos.app.jobinterviewer.flow.*
import furhatos.skills.Skill
import kotlinx.coroutines.runBlocking

class JobinterviewerSkill : Skill() {
    override fun start() {
        runBlocking {
            Init.run()
        }
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
