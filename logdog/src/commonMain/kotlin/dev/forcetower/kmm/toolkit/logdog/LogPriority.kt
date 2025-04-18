package dev.forcetower.kmm.toolkit.logdog

enum class LogPriority(val priority: Int) {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    ASSERT(7);
}