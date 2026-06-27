package model.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientStatusClient {

    private static final String CLIENT_STATUS_URL =
            "http://localhost:8080/ConneXtion_ClientApplication/api/issues/status";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void updateClientIssueStatus(String reference, String status, String resolutionComment) throws Exception {
        if (reference == null || reference.isBlank()) {
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reference", reference);
        body.put("status", status);
        body.put("resolutionComment", resolutionComment);

        String json = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLIENT_STATUS_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("Cliente respondió con estado "
                    + response.statusCode() + ": " + response.body());
        }
    }
}