package collector.bitfinex.server

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
internal open class BitfinexContext {
//    val log by logger("spring-context")
//
//    @Autowired
//    var environment: Environment? = null
//
//    @Bean
//    @ConfigurationProperties(prefix = "bitfinex")
//    open fun config(): BitfinexConfig {
//        return BitfinexConfig()
//    }
//
//    @Bean
//    open fun bitfinex(config: BitfinexConfig): Bitfinex {
//        return Bitfinex(config)
//    }
//
//    @Bean fun bitfinexServer(bitfinex: Bitfinex): Server {
//        return BitfinexService(bitfinex)
//    }
//
//    @PostConstruct
//    fun init() {
//        // if in production email errors
//        for (profile in environment!!.activeProfiles) {
//            log.info("registering failure handler (production mode)")
//            if (profile == "prod") {
//                exceptionUtils.onUnrecoverableFailure { throwable -> Mailer.alert("unrecoverable error", throwable.stackTraceAsString()) }
//                break
//            }
//        }
//    }
}