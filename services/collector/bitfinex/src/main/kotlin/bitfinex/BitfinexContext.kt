package bitfinex

import com.tars.util.exceptions.ExceptionUtils
import com.tars.util.net.messenger.Mailer
import extensions.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import util.cpu
import util.net
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@SpringBootApplication
internal open class BitfinexContext {
    val log by logger()

    @Autowired
    var environment: Environment? = null

    @Bean
    @ConfigurationProperties(prefix = "bitfinex")
    open fun config(): Config {
        return Config()
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    open fun bitfinex(config: Config): Bitfinex {
        return Bitfinex(config)
    }

    // lifecycle

    @PostConstruct
    fun init() {
        cpu.init()
        net.init()

        // if in production report failures
        for (profile in environment!!.activeProfiles) {
            log.info("registering failure handler (production mode)")
            if (profile == "prod") {
                ExceptionUtils.onUnrecoverableFailure { throwable -> Mailer.alert("unrecoverable error", ExceptionUtils.stackTraceAsString(throwable)) }
                break
            }
        }
    }

    @PreDestroy
    fun destroy() {
        net.close()
        cpu.close()
    }
}