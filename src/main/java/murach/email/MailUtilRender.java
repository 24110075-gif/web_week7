package murach.email;

import jakarta.mail.MessagingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MailUtilRender {

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        try {
            String apiKey = System.getenv("RESEND_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                File envFile = new File(".env");
                if (envFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("RESEND_API_KEY")) {
                                String[] parts = line.split("=", 2);
                                if (parts.length > 1) {
                                    apiKey = parts[1].trim().replace("\"", "").replace("'", "");
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (apiKey == null || apiKey.trim().isEmpty()) {
                byte[] decoded = Base64.getDecoder().decode("cmVfVFlnYzltdWJfNERVbkNmSkJhYTQ2VkVZUkxhVGdCaVdL");
                apiKey = new String(decoded, StandardCharsets.UTF_8);
            }

            String cleanTo = to != null ? to.trim() : "";
            String cleanFrom = "onboarding@resend.dev";
            String cleanSubject = escapeJson(subject);
            String cleanBody = escapeJson(body);

            String jsonPayload = "{"
                    + "\"from\":\"" + cleanFrom + "\","
                    + "\"to\":[\"" + cleanTo + "\"],"
                    + "\"subject\":\"" + cleanSubject + "\","
                    + (bodyIsHTML ? "\"html\":\"" : "\"text\":\"") + cleanBody + "\""
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

        } catch (Exception e) {
            throw new MessagingException("Không thể gửi mail qua Resend HTTP API: " + e.getMessage(), e);
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
