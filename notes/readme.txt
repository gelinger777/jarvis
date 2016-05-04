
Run task "projects" to see list of projects with their descriptions.

Some general conventions to have in mind. Jarvis is multi module gradle build, we split application to microservices, and try to keep them too simple to fail. 

If something is logical to split into a module it is being separated to a module even if thats just one class...

Overall trading system is composed of the following major components

    1. Collectors : each collector is a microservice (an app) that runs independently and is collecting data from a single exchange,
                    we create a collector per exchange, so if we need historical trade/orderbook data for bitfinex, then
                    bitfinex-collector-server is the project that is taking care of connecting to the exchange and collecting data
                    to some persistent storage... and only that. In order to communicate with that microservice we have its client
                    a separate project (a lib) which we can use to connect and send commands for which markets to collect from etc...

    2. EventStore : this is our storage for most of our data, historical trades, orderbook and any other data that might have form of
                    an event stream...

    3. Indicators : these are (a lib) projects that can take a source of data (for example trade stream, or orderbook stream or both plus something else)
                    and will give another stream in the end. This way you can dynamically compose different indicators with different parameters. For example
                    if you need 10 minute OHLC (Open High Low Close) data, you can take a raw trade stream (realtime or historical) and apply
                    ohlc(10, Minute) and get a stream of data, each element will represent OHLC for 10 minute time frame...

    4. Strategies : Strategies use sources of data, apply some indicators, and execute a logic that is depending on indicators... For example when we want to
                    test our strategy on historical data, we query it from EventStore get a realtime stream, then project this data trough some indicators,
                    then we apply strategy logic... If strategy works for production it will directly consume actual data stream instead of historical data...
                    Or for example we want to check if there is a correlation between two different market data, we compose a stream of indicator which takes
                    two sources and give one stream of correlation value...


Technological stack
    Java8/kotlin
    Gradle
        Gradle build all the applicaitons to /dist/APPNAME
        Configurations for them to use when starting are stored at /dist/conf
        Current configurations will direct data storage root to /dist/data
        All the logs are going to /dist/logs
    Protobuf/GRPC
        All the communication between microservices.
    OpenHFT/ChronicleQueue
        This is fastest append only solution for storing event streams.