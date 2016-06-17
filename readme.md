# Jarvis - Autonomous Trading System

Jarvis is a set of modules that together provide necessary environment to create any trading strategies. Codebase is created so that its very readable and flexible. Support for exchange, indicator or strategy can be easily implemented extending existing codebase or implementing by convention.

We use multi module gradle build with single file configuration. Modules and their dependencies are described in `build.gradle`.

Some modules are libraries, others are runnable services in which case `gradle` will produce distribution under `/dist` folder with the service name and necessary binaries. Services (with few exceptions) provide two modules one for client and one for server. Client module only provides necessary classes to connect to server and use the provided api. Server modules only expose a runnable class that can be used to run the service, implementation details shall always be isolated.

All services are communicating using `HTTP2` over `GRPC` which provides bidirectional communication.

All data structures are defined using `Protobuf` which generates fast and memory efficient serialization/deserialization logic for each data structure. Those are used to serialize data and persist to event streams. Due to very compact size huge amount of data can be stored and can easily be transferred.

We use `eventstore`, a module that is providing storage service for denormalized data, specifically event streams, it is optimized for sequential writes and reads.


## Tools

Before working with this project make sure you have following tools configured properly.
* `intellij ultimate` there are some plugins that are available only for ultimate edition,
make sure the following plugins are installed
    * `Markdown` to read and edit `*.md` files.
    * `Protobuf Support` both protobuf and grpc highlighting.
    * `NodeJS` communication hub between java services and browsers.
* `sublime text` for some tasks sublime may be more appropriate (such as UI and notes), make sure following plugins are installed
    * `PlainTasks` to open `tasks.todo` file.
    * `Typescript`
    * `Handlebars`
    * `SASS`
    * `SidebarEnhancements`


## Design


 Working trading system is a composition of several services and libraries. Those can be logically divided to several categories :

 * `collectors` handle historical data collection, each collector is responsible for one exchange, these must be very reliable as any interruption of collected data makes it unusable for testing. Collection must not be interrupted or have data loss.
 * `indicators` take some source of data like stream of trades or orderbook stream and extract some meaningful information about it. They process data in realtime as soon as it arrives. There is no need to store data that is projected using indicators. General notes regarding indicators can be found in `/notes/indicators.txt` file.
 * `strategies` may use indicators to generate trading signals, these contain decision making logic based on information that is being provided by some set of indicators, each strategy defines required set of indicators. Strategy may also use historical data to learn and optimize for specific market.
 * `other` components such as `eventstore` are there to facilitate work of the logical components.

Intention is to keep them so small and simple that it is not necessary to write complex tests and very easy to maintain/test using few scripts. This is why we use `internal/tryout-*.kt` files in each project.

All the logging configuration for all project is centralized to one configuration, no matter what project you run make sure that environment variable `log4j.configuration` is passed in environment variables.

## Technological Stack


We use latest stable versions of the best tools available relevant for the tasks we solve.

* Backend
    * `Kotlin` - as language of choice.
    * `Gradle` - for all the complex build tasks.
    * `Protobuf` - to generate all data structures.
    * `GRPC` - to define interfaces of all services.
    * `RxJava` - to work with observable streams.
    * `Chronicle` - used for persistence of data streams.

* Frontend
    * `Node` - as endpoint for ui websocket communication.
    * `Angular 2` - eventstore.client.common.main framework for ui.
    * `Material Components` - material design as base.
    * `TypeScript` - better js and default for angular 2.
    * `D3` - for charts.


## Folder Structure


* `/sources` contains all the source files for all the modules without any build configurations and scripts
* `/notes` general notes about different aspects of the project, `.todo` files
are used with `sublime text` (with `PlainTasks` plugin enabled)
* `/dist` default location for all the artifacts
    * `/bin` binary distributions of different services
    * `/conf` all the configurations for different services


## Conventions

All services follow some conventions that proved to be good practice over time. `Java/kotlin` apps use shared `:global:util` lib which has some utility classes that handle some routine tasks. More information is provided in project `readme.md`.

All the text in `readme` files or `notes` are single line per paragraph, make sure to turn on `soft wrap` line breaks for the `idea` and `sublime` to automatically adjust to your screen size.

For faster code assistance in `Editor > General > Code Completion` set case sensitive completion to all, autopopup times to 1ms...

By default generated binary distribution for services are stored in `/dist/bin`, and configurations in `/dist/conf` folder, those can be overridden in `build.gradle`. Feel free to customize those folders to fit your workspace and use your api keys in config json files, these changes shall never be pushed to remote repositories but must be kept in your private development branch...

Each `app` expects following environment variables to be provided :
* `conf` location of json configuration for that specific app
* `log.path` where log files for the execution will be stored
* `profile` profile information is available in runtime

Each module that may need some explanation will provide a `readme.md` file under its sources root.

## Run Instructions

In order to recompile `.proto` files run

    ./gradlew recompileProto

That will delete previously generated files, and regenerate them again.

In order to generates binaries for services run

    ./gradlew installDist

This will generate binary distributions of services and store them under `/dist/app/..`

Shell scripts under `/bin` directory have default environment variables described in `build.gradle`, those shall be be customized for local development or production environment. For that make a separate branch, change the files accordingly and leave those changes.

## Contributing

If multiple devices are used to work with the codebase a developer can keep a single dev branch on remote repo like `dev-vach` and all the unfinished work is being committed to that branch when changing different development environments... When a feature is complete `squash` command can be used to merge all changes to the `eventstore.client.common.main` branch.

```
    git merge --squash dev-vach
```

This will merge all work (many commits) from `dev-vach` to `eventstore.client.common.main`, but without committing it. At this point developer can do final polish of the source and make a single commit.

Code quality, readability and modularity are a top priority in this project hence in order to separate boilerplate from actual business logic code, we use extensions  in file `/internal/boilerplate.kt` in the root package of each module, each method or object defined in `boilerplate.kt` must have `internal` visibility, this file is only for implementation boilerplate, it shall not contain valuable business logic on its own, usually all the code that one must never read trough must be here...

`internal` package can also be used to hold some sample run scripts that are not valid tests on their own but are valuable for development (for debugging purposes), those are stored in `tryout-this-or-that.kt` files and everything inside is declared with `private` visibility.