package com.respawn;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransactionPoller {

    private static final String ENDPOINT_URL = "https://api.ggleap.com/beta/transactions/history";
    private static final String AUTH_URL = "https://api.ggleap.com/production/authorization/public-api/auth";
    private static final String AUTH_TOKEN = "SuperSecretPassword";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private static String getJWT(HttpClient client) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(AUTH_URL))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"AuthToken\": \"" + AUTH_TOKEN + "\"}"))
            .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String body = readBody(response.body());

        if (response.statusCode() != 200) {
            throw new Exception("JWT POST fail : " + response.statusCode() + " " + preview(body));
        }

        JsonNode jwt = mapper.readTree(body).get("Jwt");
        if (jwt == null || jwt.isNull()) {
            throw new Exception("JWT POST fail : no Jwt in response " + preview(body));
        }
        System.out.println("\nJWT success");
        return jwt.asText();
    }

    public static String pollApi(HttpClient client) throws Exception {
        String jwt = getJWT(client);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ENDPOINT_URL))
            .timeout(Duration.ofSeconds(30))
            .header("Authorization", "Bearer " + jwt)
            .header("Accept", "application/json")
            .header("Accept-Encoding", "gzip")
            .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String body = readBody(response.body());

        if (response.statusCode() != 200) {
            throw new Exception("Transactions GET fail : " + response.statusCode() + " " + preview(body));
        }
        System.out.println("Response Received! " + response);
        return body;
    }

    /* Check bytes for GZIP to avoid error */
    private static String readBody(byte[] bytes) throws Exception {

        boolean isGzip = bytes.length >= 2
            && (bytes[0] & 0xFF) == 0x1F
            && (bytes[1] & 0xFF) == 0x8B;
        if (!isGzip) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        System.out.println("IS GZIP");
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String preview(String s) {
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }


}