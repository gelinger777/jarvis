package common

/**
 * Defines cleanup priorities for different resources of the application.
 * Tasks submitted with cleanup.addTask(..) that have higher priority will execute first.
 */
internal enum class CleanupPriority(val priority: Int) {
    Watcher(1),
    CHRONICLE(2)
}