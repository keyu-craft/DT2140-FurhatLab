package furhatos.app.hotelreceptionist.flow

import furhatos.flow.State
import furhatos.flow.state
import furhatos.rtapi.v1.Language
import furhatos.flow.furhat

val Init: State = state {

    onEntry {

        receptionistGenerator.LLM.warmup()
        managerGenerator.LLM.warmup()
        furhat.enableAutoGestures()
        furhat.users.setEngagementPolicy(innerDistance = 1.5, outerDistance = 2.0)

        // Enable this if you want to automatically insert a virtual user
        //if (furhat.isVirtual && furhat.users.all.isEmpty()) {
        //    logger.info("Furhat is virtual, adding a virtual user")
        //    furhat.users.addVirtualUser()
        //}

        furhat.setVoice(language = Language.ENGLISH_US)

        goto(Idle)
    }
}
