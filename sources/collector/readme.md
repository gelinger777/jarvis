# Collectors

Purpose of all collector services is to collect data for their specific exchange, so for example `bitfinex-collector` is a separate microservice that collects data for `bitfinex` exchange.

All collector servers are implementing `/common/CollectorGRPC/Collector`. As all collectors implement the same generic interface the client for working with all of them is one and is under `/collector/client`.

`collector-common` project contains all the necessary code to create new collectors using `client` and `event-store` instances. This project is reused in all collectors.