package util.network.mail

import net.sargue.mailgun.Configuration
import net.sargue.mailgun.MailBuilder
import util.global.logger

class Postman(
        val domain: String = "sandboxbfb47b35ceda4ea7b6da990203cb3351.mailgun.org",
        val apiKey: String = "key-e3151e2169e042de2bf2c21d05171cb8"
) {

    private val log = logger("Postman")

    val config = Configuration()
            .domain(domain)
            .apiKey(apiKey)

    fun send(subject: String, message: String,
             destination: String ,
             senderName: String ,
             senderAddress: String
    ) {
        log.info { "sending an email to [$destination]" }

        MailBuilder.using(config)
                .from(senderName, senderAddress)
                .to(destination)
                .subject(subject)
                .text(message)
                .build()
                .send();
    }
}