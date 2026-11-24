package furhatos.app.hotelreceptionist.flow

import furhatos.flow.State
import furhatos.flow.onUserEnter
import furhatos.flow.state
import furhatos.flow.furhat

val Idle: State = state {

    onEntry {
        if (furhat.users.engaged.isNotEmpty()) {
            goto(Receptionist)
        } else {
            furhat.attendNobody()
            furhat.stopSpeaking()
        }
    }

    onUserEnter {
        goto(Receptionist)
    }

}
