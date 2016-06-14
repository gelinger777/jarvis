# :global:eventstore [app]

Module provides utilities to operate persistence, storage and processing of historical data without any centralized service. We store data, and later decide how we want to use it.

It provides different components which handle very specific tasks but in composition can solve range of problems in their domain.

* `StreamWriter` : allows to append byte[] to chronicle queue with some rollup interval.
* `StreamUploader` : given a stream and rollup interval, it will keep checking for new data files, and will upload to remote data storage.
* `StreamPoller` : scans all available streams on remote storage, and allows to download to the host machine.
* `SpecializedReaders` : these readers analyse the raw stream of data and maintain in memory index. They are implemented outside of the module as they represent application specific logic...
    * `TradeStreamReader` : analyses and indexes raw stream of trades.
    * `OrderStreamReader` : analyses and indexes raw stream of orders. Unlike trades, orderbook streaming does not make sense without initial orderbook being sent, for that purpose it will create snapshots for each rolled up data file. When queried for order stream, it will initially stream orderbook at that point of time and other orders after that.

