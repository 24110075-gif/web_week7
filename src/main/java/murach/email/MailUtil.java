package murach.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MailUtil {

    public static void sendMail(String to, String from,
            String subject, String body, boolean isBodyHTML)
            throws MessagingException {

        String resendKey = getEnv("RESEND_API_KEY");
        if (resendKey != null && !resendKey.isEmpty()) {
            try {
                sendViaResend(to, from, subject, body, isBodyHTML, resendKey);
                return;
            } catch (Exception e) {
                System.err.println("Gửi mail qua Resend API thất bại: " + e.getMessage());
            }
        }

        String brevoKey = getEnv("BREVO_API_KEY");
        if (brevoKey != null && !brevoKey.isEmpty()) {
            try {
                sendViaBrevo(to, from, subject, body, isBodyHTML, brevoKey);
                return;
            } catch (Exception e) {
                System.err.println("Gửi mail qua Brevo API thất bại: " + e.getMessage());
            }
        }

        sendViaSmtp(to, from, subject, body, isBodyHTML);
    }

    private static void sendViaResend(String to, String from, String subject, String body, boolean isHTML, String apiKey) throws Exception {
        String cleanTo = to.trim();
        String cleanFrom = "onboarding@resend.dev";
        String cleanSubject = escapeJson(subject);
        String cleanBody = escapeJson(body);

        String jsonPayload = "{"
                + "\"from\":\"" + cleanFrom + "\","
                + "\"to\":[\"" + cleanTo + "\"],"
                + "\"subject\":\"" + cleanSubject + "\","
                + (isHTML ? "\"html\":\"" : "\"text\":\"") + cleanBody + "\""
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new MessagingException("Lỗi Resend API (Mã " + response.statusCode() + "): " + response.body());
        }
    }

    private static void sendViaBrevo(String to, String from, String subject, String body, boolean isHTML, String apiKey) throws Exception {
        String cleanTo = to.trim();
        String cleanFrom = (from != null && from.contains("@")) ? from.trim() : "noreply@example.com";
        String cleanSubject = escapeJson(subject);
        String cleanBody = escapeJson(body);

        String jsonPayload = "{"
                + "\"sender\":{\"email\":\"" + cleanFrom + "\"},"
                + "\"to\":[{\"email\":\"" + cleanTo + "\"}],"
                + "\"subject\":\"" + cleanSubject + "\","
                + (isHTML ? "\"htmlContent\":\"" : "\"textContent\":\"") + cleanBody + "\""
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new MessagingException("Lỗi Brevo API (Mã " + response.statusCode() + "): " + response.body());
        }
    }

    private static void sendViaSmtp(String to, String from, String subject, String body, boolean isBodyHTML) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        final String username = getEnvOrDefault("GMAIL_USER", "cutcho385@gmail.com");
        final String password = getEnvOrDefault("GMAIL_PASS", "mhgw wxgk erjt cjes");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        if (isBodyHTML) {
            message.setContent(body, "text/html; charset=UTF-8");
        } else {
            message.setText(body);
        }

        Transport.send(message);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String val = getEnv(key);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private static String getEnv(String key) {
        String val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#") || !line.contains("=")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts[0].trim().equalsIgnoreCase(key)) {
                        String v = parts[1].trim();
                        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                            v = v.substring(1, v.length() - 1);
                        }
                        return v;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
}
