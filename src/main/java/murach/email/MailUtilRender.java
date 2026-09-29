package murach.email;

import jakarta.mail.MessagingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
                    } catch (Exception ignored) {}
                }
            }

            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new MessagingException("Thiếu biến môi trường RESEND_API_KEY.");
            }

            String cleanTo = to.trim();
            String cleanFrom = "onboarding@resend.dev";
            String cleanSubject = subject.replace("\\", "\\\\").replace("\"", "\\\"");
            String cleanBody = body.replace("\\", "\\\\")
                                   .replace("\"", "\\\"")
                                   .replace("\n", "\\n")
                                   .replace("\r", "");

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
}
