package com.tars.util.net.messenger;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.MailBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tars.util.validation.Validator.allNotNullOrEmpty;
import static com.tars.util.validation.Validator.condition;

public class Mailer {

  private static final Logger log = LoggerFactory.getLogger("mailer");

  private static final Configuration configuration = new Configuration()
      .domain("sandboxbfb47b35ceda4ea7b6da990203cb3351.mailgun.org")
      .apiKey("key-e3151e2169e042de2bf2c21d05171cb8")
      .from("Tars", "tars@tars.com");

  public static void alert(String subject, String message) {
    condition(allNotNullOrEmpty(subject, message));

    String destination = "vachagan.balayan@gmail.com";

    log.info("sending an email to {} with subject {}", destination, subject);

    MailBuilder.using(configuration)
        .to(destination)
        .subject(subject)
        .text(message)
        .build()
        .send();
  }

  public static Runnable createEmailCallback(String subject, String message) {
    condition(allNotNullOrEmpty(subject, message));
    return () -> alert(subject, message);
  }

//  public static void main(String[] args) {
//    alert("bitfinex is fucked", "does not collect anymore");
//  }
}
