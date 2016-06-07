# :engine

Engine is an abstract component that has depends on all other components in the system, which is why we put all integration `tryout` scripts in this module.

For example it contains `tryout-bitfinex-collector-*` scripts that will first run `evenstore` instance, then `bitfinex-collector` then will execute specific logic using those. Individually those components don't have the necessary visibility to do that.

Engine is a temporary project that will be splitted into different services later, services like `risk-managemnet`, `bot-*` etc..