package util

import extensions.logger
import net.sargue.mailgun.Configuration
import net.sargue.mailgun.MailBuilder

object mailer {
    private val log by logger()

    val config = Configuration()
            .domain("sandboxbfb47b35ceda4ea7b6da990203cb3351.mailgun.org")
            .apiKey("key-e3151e2169e042de2bf2c21d05171cb8")
            .from("Tars", "tars@tars.com")

    fun send(subject: String, message: String, destination: String = "vachagan.balayan@gmail.com") {
        log.info("sending an email to [$destination]");

        MailBuilder.using(config)
                .to(destination)
                .subject(subject)
                .text(message)
                .build()
                .send();
    }
}