# Clients package

Contains libraries for different exchanges, these libraries are translating specific exchange APIs
to generic api that we use in the system and store data.

## Conventions

The system treats orderbooks as stream of orders, and it does not hold raw orders but aggregates
them by price, so for example two orders that arrive one after another but have same exact price,
will be aggregated to single order. The event stream will see 2 events, order was created, and order
was modified. The modified order in reality may be a different order that was posted with the same
price. Order cancellation event is an order event that has the same price but 0 volume.