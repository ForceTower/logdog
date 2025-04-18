package dev.forcetower.kmm.toolkit.logdog

fun Throwable.asLog(): String {
    return stackTraceToString()
}