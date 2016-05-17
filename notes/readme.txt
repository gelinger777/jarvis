------------------------------------------------------------------------------------------------------
    Intro
------------------------------------------------------------------------------------------------------


Aim of the project is to provide automatically adjusting trading system, with risk/portfolio management.

Some general conventions to have in mind. Jarvis is multi module gradle build, we split application to microservices, and keep them too simple to fail. 
Services communicate using GRPC HTTP2 connections (which allow full duplex communication). No crappy old REST allowed here...

If something is logical to split into a module it is being separated to a module even if that's just one class... 
We keep using latest versions of the best tools for the job, no legacy shit is allowed here only cool stuff...

Code quality is a priority, make sure that business logic is perfectly readable, create as many utilities as you need for that...

Overall trading system is composed of the following major component types, there might be multiple instances of these : 

    1. Collectors : each collector is a microservice (an app) that runs independently and is collecting data from a single exchange,
                    we create a collector per exchange, so if we need historical trade/orderbook data for bitfinex, then
                    bitfinex-collector-server is the project that is taking care of connecting to the exchange and collecting data
                    and only that. In order to communicate with that microservice we have its client a separate project (a lib)
                    which we can use to connect and send commands for which markets to collect from or stop collection etc...

    2. EventStore : this is our storage for most of our data, historical trades, orderbook and any other data that might have form of
                    an event stream... It is highly optimized by all means, messages are serialized with protobuf which means we deal with several bytes per message,
                    those are appended to memory mapped file (it is being persisted to hard drive by the OS). Hence it is extremely memory efficient and super fast as
                    when we write or read we operate with off heap memory not the hard drive also serialization and deserialization are done by protobuf and
                    communication with other services are provided by grpc (both are best tools available for the task)...
                    EventStore only takes care of storing and streaming data via Observable streams, which means we process data while its being streamed to our client...

    3. Indicators : these are (a lib) projects that can take a source of data (for example trade stream, or orderbook stream or both plus something else)
                    and will give another stream in the end. This way you can dynamically compose different indicators with different parameters. For example
                    if you need 10 minute OHLC (Open High Low Close) data, you can take a raw trade stream (realtime or historical) and apply
                    OHLC(10, Minute) and get an observable stream of OHLC data with 10 minute time frames...

    4. Strategies : Strategies use sources of data, apply indicators, and execute a logic that is depending on values emitted by those indicators...
                    When we want to test our strategy on historical data, we query it from EventStore get a realtime stream and handle it as it is streamed in
                    realtime from an exchange, strategy cannot differentiate between historical or realtime data. Strategy composes several sources of data using
                    indicators, and uses their values to make trading decisions... Some can compose decision trees via supervised learning and let the tree to
                    adjust to given data (market agnostic algorithm) we never hard code any numbers into a strategy...


Services are depending on each other to work properly, for example as collector needs to store data, event store service
must be up and running before collector app can be started.


------------------------------------------------------------------------------------------------------
    Technological stack
------------------------------------------------------------------------------------------------------


Java8/kotlin
Gradle
    Run task "projects" to see list of projects with their descriptions.

    Gradle builds all apps (services) to /dist/$app directory.

    Configurations for them to use when starting are stored at /dist/conf,
    currently all configurations are stored in all.json but will be separated for final deployment per application.
    API keys are real but revoked from any withdrawal/order permissions so don't even try it...

    Current configurations will direct data storage root to /dist/data

    All the logs are going to /dist/logs
Protobuf/GRPC
    All the communication between services is done trough protobuf messages over grpc channels.
OpenHFT/ChronicleQueue
    This is fastest append only solution for storing event streams. This is designed for HFT systems,
    for inter process communication.

Tools
    Intellij 16 EAP
        Best support for kotlin/gradle/protobuf/everything...
    Sublime Text
        PlainTasks plugin for .todo files and notes.