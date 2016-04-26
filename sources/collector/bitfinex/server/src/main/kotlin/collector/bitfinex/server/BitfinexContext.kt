package collector.bitfinex.server

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
internal open class BitfinexContext {
//    val log by logger()
//
//    @Autowired
//    var environment: Environment? = null
//
//    @Bean
//    @ConfigurationProperties(prefix = "bitfinex")
//    open fun config(): Config {
//        return Config()
//    }
//
//    @Bean(initMethod = "start", destroyMethod = "stop")
//    open fun bitfinex(config: Config): Bitfinex {
//        return Bitfinex(config)
//    }
//
//    // lifecycle
//
//    @PostConstruct
//    fun init() {
//        // if in production email errors
//        for (profile in environment!!.activeProfiles) {
//            log.info("registering failure handler (production mode)")
//            if (profile == "prod") {
//                onUnrecoverableFailure { throwable -> Mailer.alert("unrecoverable error", throwable.stackTraceAsString()) }
//                break
//            }
//        }
//    }
}