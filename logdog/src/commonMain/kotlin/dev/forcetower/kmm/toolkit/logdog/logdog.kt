package dev.forcetower.kmm.toolkit.logdog

/**
 * A tiny Kotlin API for cheap logging on top of Android's normal `Log` class.
 *
 * The [logdog] function has 3 parameters: an optional [priority], an optional [tag], and a required
 * string producing lambda ([message]). The lambda is only evaluated if a logger is installed and
 * the logger deems the priority loggable.
 *
 * The priority defaults to [LogPriority.DEBUG].
 *
 * The tag defaults to the class name of the log call site, without any extra runtime cost. This works
 * because [logdog] is an inlined extension function of [Any] and has access to [this] from which
 * it can extract the class name. If logging from a standalone function which has no [this], use the
 * [logdog] overload which requires a tag parameter.
 *
 * The [logdog] function does not take a [Throwable] parameter. Instead, the library provides
 * a Throwable extension function: [Throwable.asLog] which returns a loggable string.
 *
 * ```
 * import dev.forcetower.kmp.toolkit.LogPriority.INFO
 * import dev.forcetower.kmp.toolkit.asLog
 * import dev.forcetower.kmp.toolkit.logdog
 *
 * class MouseController {
 *
 *   fun play {
 *     var state = "CHEEZBURGER"
 *     logdog { "I CAN HAZ $state?" }
 *     // logdog output: D/MouseController: I CAN HAZ CHEEZBURGER?
 *
 *     logdog(INFO) { "DID U ASK 4 MOAR INFO?" }
 *     // logdog output: I/MouseController: DID U ASK 4 MOAR INFO?
 *
 *     logdog { exception.asLog() }
 *     // logdog output: D/MouseController: java.lang.RuntimeException: FYLEZ KERUPTED
 *     //                        at sample.MouseController.play(MouseController.kt:22)
 *     //                        ...
 *
 *     logdog("Lolcat") { "OH HI" }
 *     // logdog output: D/Lolcat: OH HI
 *   }
 * }
 * ```
 *
 * To install a logger, see [LogdogLogger].
 */
inline fun Any.logdog(
    priority: LogPriority = LogPriority.DEBUG,
    /**
     * If provided, the log will use this tag instead of the simple class name of `this` at the call
     * site.
     */
    tag: String? = null,
    message: () -> String
) {
    LogdogLogger.logger.let { logger ->
        if (logger.isLoggable(priority)) {
            val tagOrCaller = tag ?: outerClassSimpleNameInternalOnlyDoNotUseKThxBye()
            logger.log(priority, tagOrCaller, message())
        }
    }
}

/**
 * An overload for logging that does not capture the calling code as tag. This should only
 * be used in standalone functions where there is no `this`.
 * @see logdog above
 */
inline fun logdog(
    tag: String,
    priority: LogPriority = LogPriority.DEBUG,
    message: () -> String
) {
    with(LogdogLogger.logger) {
        if (isLoggable(priority)) {
            log(priority, tag, message())
        }
    }
}

@PublishedApi
internal fun Any.outerClassSimpleNameInternalOnlyDoNotUseKThxBye(): String {
    val javaClass = this::class
    val fullClassName = javaClass.qualifiedName ?: "Unknown"
    val outerClassName = fullClassName.substringBefore('$')
    val simplerOuterClassName = outerClassName.substringAfterLast('.')
    return if (simplerOuterClassName.isEmpty()) {
        fullClassName
    } else {
        simplerOuterClassName.removeSuffix("Kt")
    }
}