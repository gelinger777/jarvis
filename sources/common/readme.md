# :common [lib]

Contains all the codebase that is shared across different modules.

Common data structures and interfaces are defined in `common.proto`.

EventStore interface is defined in `event-store.proto`.

Note that every time any modification is made to `.proto` file, `recompileProto` task must be invoked.

    ./gradlew recompileProto

