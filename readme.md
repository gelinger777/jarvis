# Jarvis - Autonomous Trading System

Jarvis is a set of modules that together provide necessary environment
to create any trading strategy imaginable. Codebase is created so that
its very readable and flexible. Support for exchange, indicator or strategy
can be easily implemented using existing codebase.

We use multi module gradle build with single file configuration. Modules
and their dependencies are described in `build.gradle`.

Some modules are libraries, some are runnable services in which case
`gradle` will produce distribution under `/dist` folder with the service name
and necessary binaries. Services provide two modules one for client and
one for server.

All services are communicating using `HTTP2` over `GRPC` which provides bidirectional
communication.

All data structures are defined using `Protobuf` which generates fast and very memory
efficient serialization/deserialization logic for each data structure. Those are used
to persist to event streams. Due to very compact size huge amount of data can be
stored and used later.

We use `eventstore` for denormalized data storage as it very efficiently stores data for sequential
access and provides streaming api.


# Tools

Before working with this project make sure you have following tools configured properly.
* `intellij ultimate` there are some plugins that are available only for ultimate edition,
make sure the following plugins are installed
    * `Markdown` to read and edit files like this (`*.md`) with proper assistance
    * `Protobuf Support` both protobuf and grpc highlighting
    * `NodeJS`
* `sublime text` for some tasks sublime may be more appropriate (such as UI codebase),
make sure following plugins are installed
    * `PlainTasks` to open `tasks.todo` file.
    * `Typescript`
    * `Handlebars`
    * `SASS`
    * `SidebarEnhancements`


# Design


 Working trading system is a composition of several services and libraries. Those
 can be logically divided to several categories :

 * `collectors` handle historical data collection, each collector is responsible
 for one exchange, these must be very reliable as any interruption of collected data
 makes it unusable for testing. Collection must not be interrupted or have data loss.
 * `indicators` take some source of data like stream of trades or
 orderbook stream and extract some meaningful information about it. They
 process data in realtime as soon as it arrives. General notes regarding indicators
 can be found in `indicators.rm` file.
 * `strategies` may use indicators to generate trading signals, these contain decision
 making logic based on information that is being provided by some set of indicators,
 each strategy defines required set of indicators. Strategy may also use historical data
 to learn and optimize for specific market.
 * `other` components are there to facilitate work of the logical components.


# Technological Stack


We use latest stable versions of the best tools available relevant for
the tasks we solve.

* Backend
    * `Java 8 / Kotlin` - main programming languages
    * `Gradle` - for all the complex build tasks
    * `Protobuf` - to generate all data structures
    * `GRPC` - to define interfaces of all services
    * `RxJava` - to work with observable streams
    * `Chronicle` - used for persistence of data streams

* Frontend
    * `Node` - as endpoint for ui websocket communication
    * `Angular 2` - main framework for ui
    * `TypeScript` - better js and default for angular 2
    * `D3` - for charts


# Folder Structure


* `/sources` contains all the source files for all the modules without any build
configurations and scripts
* `/notes` general notes about different aspects of the project, `.todo` files
are used with `sublime text` (with `PlainTasks` plugin enabled)
* `/dist` all the artifacts from build
    * `/app` binary distributions of different services
    * `/config` all the configurations for different services
    * `/data` this is the convention for storage location, all the data streams
    persisted by eventstore are located here by default


# Conventions

All services follow some conventions that proved to be good practice over time.
`Java/kotlin` apps use shared `:global:util` lib which has some utility classes that
handle some routine tasks. More information is provided in project 'readme.md'.

By convention all the generated binary distribution for any `app` will contain scripts,
that will provide the configuration location in `/config` folder, those can be overridden
if appropriate configuration is provided.

Each `app` expects following environment variables to be provided :
* `[appName]Config` location of json configuration for that specific app
* `logPath` where log files for the execution will be stored
* `profile` one of `dev` or `prod`, if the app is running in production mode, unrecoverable
failures will or failed (timeout) heartbeats will trigger an email to the developer etc...

We favour fail fast approach, if any unexpected behaviour of the application is
taking place we kill and report the cause of the failure (for example email to developer).

Each module that may need some explanation will provide a `readme.md` file under the sources root.

# Run Instructions

In order to recompile `.proto` files run

    ./gradlew recompileProto

That will delete previously generated files, and regenerate them again.

In order to generates binaries for services run

    ./gradlew installDist

This will generate binary distributions of services and store them under `/dist/app/..`

Shell scripts under `/bin` directory have default environment variables hardcoded in them,
those can be customized for production.