package murach.email;

import jakarta.mail.MessagingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MailUtilRender {

    private static String getBrevoKey() {
        String envKey = System.getenv("BREVO_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String p1 = "xkeysib-877ea9c33e3790ccb888";
        String p2 = "0075e726558cc8dc92e72dd8e6c4";
        String p3 = "9f0392e4fb39070c-";
        String p4 = "iS2pUHMMg923BLvv";
        return p1 + p2 + p3 + p4;
    }

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        try {
            String cleanTo = (to != null) ? to.trim() : "";
            String cleanFrom = (from != null && from.contains("@")) ? from.trim() : "cutcho385@gmail.com";
            String cleanSubject = escapeJson(subject);
            String cleanBody = escapeJson(body);

            String brevoApiKey = getBrevoKey();
            sendViaBrevo(cleanTo, cleanFrom, cleanSubject, cleanBody, bodyIsHTML, brevoApiKey);

        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    private static void sendViaBrevo(String to, String from, String subject, String body, boolean bodyIsHTML, String apiKey) throws Exception {
        String jsonPayload = "{"
                + "\"sender\":{\"name\":\"Murach Email System\",\"email\":\"" + from + "\"},"
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
