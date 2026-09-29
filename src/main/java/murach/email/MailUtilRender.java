package murach.email;

import jakarta.mail.MessagingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MailUtilRender {

    private static String getApiKey() {
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
            String apiKey = getApiKey();
            String cleanTo = (to != null && !to.trim().isEmpty()) ? to.trim() : "cutcho385@gmail.com";
            String cleanFrom = "onboarding@resend.dev";
            String cleanSubject = escapeJson(subject);
            String cleanBody = escapeJson(body);

            HttpResponse<String> response = executeResendRequest(apiKey, cleanFrom, cleanTo, cleanSubject, cleanBody, bodyIsHTML);

            if (response.statusCode() == 403 && response.body() != null && response.body().contains("only send testing emails")) {
                String testSubject = escapeJson("[Chế độ Test - Gửi đến: " + cleanTo + "] " + subject);
                String testBody = escapeJson("<b>[Lưu ý: Tài khoản Resend Test đang chuyển tiếp email này về hộp thư đăng ký]</b><br><br>" + body);
                response = executeResendRequest(apiKey, cleanFrom, "cutcho385@gmail.com", testSubject, testBody, true);
            }

            if (response.statusCode() >= 400) {
                throw new MessagingException("Lỗi Resend API (Mã " + response.statusCode() + "): " + response.body());
            }

        } catch (Exception e) {
            throw new MessagingException("Không thể gửi mail qua Resend HTTP API: " + e.getMessage(), e);
        }
    }

    private static HttpResponse<String> executeResendRequest(String apiKey, String from, String to, String subject, String body, boolean bodyIsHTML) throws Exception {
        String jsonPayload = "{"
                + "\"from\":\"" + from + "\","
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

        return client.send(request, HttpResponse.BodyHandlers.ofString());
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
