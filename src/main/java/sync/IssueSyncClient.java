package sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class IssueSyncClient {
    //gateway route para la peticion interna
    public static final String CLIENT_UPDATE_URL =
            "http://localhost:8082/ConneXtion_Gateway/gateway/client/issues/internal/update";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    //se envia solo los atributos que comparten
    public boolean sendClientUpdate(String reference, String classification,
                                    String status, String resolutionComment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference", reference);
        payload.put("classification", classification);
        payload.put("status", status);
        payload.put("resolutionComment", resolutionComment);

        try {
            String json = mapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CLIENT_UPDATE_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code >= 200 && code < 300) {
                System.out.println("[IssueSync] Synced reference " + reference + " to client. HTTP " + code);
                return true;
            }
            System.err.println("[IssueSync] Failed to sync reference " + reference
                    + " to client. URL=" + CLIENT_UPDATE_URL
                    + " HTTP " + code + " body=" + response.body());
            return false;

        } catch (Exception e) {
            System.err.println("[IssueSync] Error syncing reference " + reference
                    + " to client. URL=" + CLIENT_UPDATE_URL
                    + " error=" + e.getMessage());
            return false;
        }
    }
}
