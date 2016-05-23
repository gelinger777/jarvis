# Clients package

Contains libraries for different exchanges.

Currently we don't care about starting or closing resources,
if an instance of the client is created all the necessary resources
are automatically allocated...

We don't implement resource management...

There are no `close()` or `stop()` methods, don't waist time on those.