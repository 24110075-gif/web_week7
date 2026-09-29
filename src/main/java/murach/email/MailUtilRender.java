package murach.email;

import jakarta.mail.MessagingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MailUtilRender {

    private static String getResendKey() {
        String apiKey = System.getenv("RESEND_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey.trim();
        }
        byte[] decoded = Base64.getDecoder().decode("cmVfVFlnYzltdWJfNERVbkNmSkJhYTQ2VkVZUkxhVGdCaVdL");
        return new String(decoded, StandardCharsets.UTF_8);
    }

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        try {
            String cleanTo = (to != null) ? to.trim() : "";
            String cleanFrom = (from != null && from.contains("@")) ? from.trim() : "cutcho385@gmail.com";
            String cleanSubject = escapeJson(subject);
            String cleanBody = escapeJson(body);

            String brevoKey = System.getenv("BREVO_API_KEY");
            if (brevoKey != null && !brevoKey.trim().isEmpty()) {
                sendViaBrevo(cleanTo, cleanFrom, cleanSubject, cleanBody, bodyIsHTML, brevoKey.trim());
                return;
            }

            sendViaResend(cleanTo, cleanSubject, cleanBody, bodyIsHTML);

        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    private static void sendViaResend(String to, String subject, String body, boolean bodyIsHTML) throws Exception {
        String apiKey = getResendKey();
        String jsonPayload = "{"
                + "\"from\":\"onboarding@resend.dev\","
                + "\"to\":[\"" + to + "\"],"
                + "\"subject\":\"" + subject + "\","
                + (bodyIsHTML ? "\"html\":\"" : "\"text\":\"") + body + "\""
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
            if (response.body() != null && response.body().contains("only send testing emails")) {
                throw new MessagingException("Tài khoản Resend miễn phí chỉ cho phép gửi về email đăng ký (cutcho385@gmail.com). Để gửi tới email khác, cần thêm domain tại resend.com/domains hoặc dùng API Brevo.");
            }
            throw new MessagingException("Lỗi Resend API (Mã " + response.statusCode() + "): " + response.body());
        }
    }

    private static void sendViaBrevo(String to, String from, String subject, String body, boolean bodyIsHTML, String apiKey) throws Exception {
        String jsonPayload = "{"
                + "\"sender\":{\"email\":\"" + from + "\"},"
                + "\"to\":[{\"email\":\"" + to + "\"}],"
                + "\"subject\":\"" + subject + "\","
                + (bodyIsHTML ? "\"htmlContent\":\"" : "\"textContent\":\"") + body + "\""
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

    private static String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
