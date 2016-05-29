# Collectors

Purpose of all collector services is to collect data for their specific exchange, so for example `:bitfinex-collector` is an `app` that collects data for `bitfinex` exchange.

All collector servers are implementing `/common/CollectorGRPC/Collector`. As all collectors implement the same generic interface the client for working with all of them is one and is under `/collector/client`.