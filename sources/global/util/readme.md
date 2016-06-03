# :global:util

Util project contains tools that are useful for `java` application development.
It does not contain any `jarvis` specific codebase and can easily be distributed.

Singletons
* `cpu` contains references to `executors` and `schedulers` which must be used for any
concurrent execution in the application. Creation of any thread pools is discouraged.
When all the concurrency is handled by specific pools it is very easy to monitor efficiency
of concurrent execution throughout application lifecycle.
* `net` provides tools to operate with different network protocols, HTTP, Websocket,
TCP Sockets, Pusher etc..
* `heartBeat` helper object to declare heartbeats and define callbacks if those heartbeats
are violated (timeout reached etc...)
* `cleanupTasks` handles ordered execution of cleanup tasks, for example `net` will release
all network resources before `cpu` will shut down execution pools... Cleanup tasks can be
registered/unregistered with priorities and when application shuts down those are
executed in right order...
* `app` will initialize default logger for application, and read conventional properties
from environment variables such as `profile` and `logPath`

We favour fail fast approach, if any unexpected behaviour of the application is taking place we kill and report the cause of the failure (for example email to developer).

Fail fast approach is enforced using `wtf()` methods which throw an exception that inevitably triggers the system to shut down and execute registered cleanup tasks see `util.global.exceptions.kt`