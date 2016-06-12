# :global:eventstore [app]

EventStore is database is optimized for sequential access, and provides streaming api.

### EventStore as a service

Initial version was designed as a service with `client` and `server` modules. It is intended that instance that runs server must have vast amount of storage available.

All streaming between clients and servers supports backpressure, messages will be arranged to batches between transmission over the wire, depending on which side is slower that side will aquire batches of events instead of individual events. If both sides have efficient batch processing then backpressure is solved out of the box.

### Stream processing tools

Unlike service approach `tools` module provides utilities to operate persistence, storage and processing of historical data without any centralized service. We store data, and later decide how we want to use it, while with storage as a service, we inevitably have a moment when we run out of storage or code gets outdated and we need to redeploy or update the service (which means interruptions in work or a complicated synchronization between old and new deployments).

With this package there is no interruption as persistence and storage (production) are separated from reading and processing tasks (consumption), wheres with service we have to upgrade hardware or redeploy the service to deliver new code and functionality.

It provides different components which handle very specific tasks but in composition can solve range of problems in their domain.

* `StreamWriter` : allows to append byte[] to chronicle queue with some rollup interval.
* `StreamUploader` : given a stream and rollup interval, it will keep checking for new data files, and will upload to remote data storage.
* `StreamPoller` : scans all available streams on remote storage, and allows to download to the host machine.
* `SpecializedReaders` : these readers analyse the raw stream of data and maintain in memory index. They are implemented outside of the module as they represent application specific logic...
    * `TradeStreamReader` : analyses and indexes raw stream of trades.
    * `OrderStreamReader` : analyses and indexes raw stream of orders. Unlike trades, orderbook streaming does not make sense without initial orderbook being sent, for that purpose it will create snapshots for each rolled up data file. When queried for order stream, it will initially stream orderbook at that point of time and other orders after that.

