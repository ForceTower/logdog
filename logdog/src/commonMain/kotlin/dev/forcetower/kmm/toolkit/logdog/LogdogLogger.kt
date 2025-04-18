package dev.forcetower.kmm.toolkit.logdog

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized

interface LogdogLogger {

    /**
     * Whether a log with the provided priority should be logged and the corresponding message
     * providing lambda evaluated. Called by [logdog].
     */
    fun isLoggable(priority: LogPriority) = true

    /**
     * Write a log to its destination. Called by [logdog].
     */
    fun log(
        priority: LogPriority,
        tag: String,
        message: String
    )

    companion object {
        @PublishedApi
        internal var logger: LogdogLogger = NoLog
            private set

        private var installedThrowable: Throwable? = null
        @OptIn(InternalCoroutinesApi::class)
        private val lock = SynchronizedObject()

        val isInstalled: Boolean
            get() = installedThrowable != null

        /**
         * Installs a [LogdogLogger].
         *
         * It is an error to call [install] more than once without calling [uninstall] in between,
         * however doing this won't throw, it'll log an error to the newly provided logger.
         */
        @OptIn(InternalCoroutinesApi::class)
        fun install(logger: LogdogLogger) {
            synchronized(lock) {
                if (isInstalled) {
                    logger.log(
                        LogPriority.ERROR,
                        "LogdogLogger",
                        "Installing $logger even though a logger was previously installed here: " +
                                installedThrowable!!.asLog()
                    )
                }
                installedThrowable = RuntimeException("Previous logger installed here")
                Companion.logger = logger
            }
        }

        /**
         * Replaces the current logger (if any) with a no-op logger.
         */
        @OptIn(InternalCoroutinesApi::class)
        fun uninstall() {
            synchronized(lock) {
                installedThrowable = null
                logger = NoLog
            }
        }
    }

    private object NoLog : LogdogLogger {
        override fun isLoggable(priority: LogPriority) = false

        override fun log(
            priority: LogPriority,
            tag: String,
            message: String
        ) = error("Should never receive any log")
    }
}