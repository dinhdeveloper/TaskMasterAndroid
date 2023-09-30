package com.elogictics.taskmaster.utils.eventbus

class EventBusAction(
    val action: Action = Action.NO_THING,
    val obj: Any? = null
) {
    enum class Action {
        NO_THING,
        REFRESH_TOKEN_FB,
        CHANGE_LOGO,
    }
}