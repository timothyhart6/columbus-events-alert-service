package com.ColumbusEventAlertService.services.smsProviders;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioService implements SmsProviderService {


    public void sendTextMessage(String messageText) {
        Twilio.init(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));
        String toPhoneNumber = System.getenv("TESTING_PHONE_NUMBER");
        String fromPhoneNumber = System.getenv("TWILIO_PHONE_NUMBER");

        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(toPhoneNumber),
                        new com.twilio.type.PhoneNumber(fromPhoneNumber),
                        messageText)
                .create();

        log.info(message == null || (message.getErrorMessage() != null && !message.getErrorMessage().isEmpty())
                ? "Message failed to send. Error message: " + (message == null ? "null" : message.getErrorMessage())
                : "Message sent successfully. Message sent: " + message.getBody());
    }
}