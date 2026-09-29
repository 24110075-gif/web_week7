package murach.email;

import jakarta.mail.MessagingException;

public class MailUtil {

    public static void sendMail(String to, String from,
            String subject, String body, boolean isBodyHTML)
            throws MessagingException {

        MailUtilRender.sendMail(to, from, subject, body, isBodyHTML);
    }
}
