package com.respawn;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransactionPoller {

    private static final String ENDPOINT_URL = "https://api.ggleap.com/beta/transactions/history";
    private static final String AUTH_URL = "https://api.ggleap.com/production/authorization/public-api/auth";
    private static final String AUTH_TOKEN = "uMCkgHGyGQMBDuLTUjDGbpca8yKk1AvfRRNYoQ8vJ1kuX3k54Vr3yY9wjCckw2rOGSi+1kMDWMSnV0Yb6AStlXuFDyvJglskl/8x9cWPJruYTUn/R6GwmwmMnQXNwoNP";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private static String getJWT(HttpClient client) throws Error{
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(AUTH_URL))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"AuthToken\": \""+AUTH_TOKEN+"\"}"))
            .build();
            HttpResponse<String> response = null;
            try { 
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assert response.statusCode() == 200;
                System.out.println("\nJWT success");
                //System.out.println(response.body());
                JsonNode rootNode = mapper.readTree(response.body());
                try { 
                    return rootNode.get("Jwt").asText();
                } catch(NullPointerException npe){
                    throw new Exception("Too many Requests");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } catch (AssertionError a) {
                System.out.println("JWT POST fail : " + response);
            }
        throw new Error("Failed to obtain JWT!");
    }

    /*
     * Gets transactions history from API, in future could use filtered transactions but currently throws 500
     *  
     */
    public static String pollApi(HttpClient client) throws Exception {
        String JWT = getJWT(client);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ENDPOINT_URL))
            .header("Authorization", "Bearer "+JWT)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build();

        HttpResponse<byte[]> response = null;
        try { 
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            assert response.statusCode() == 200;
            System.out.println("Response Received! " + response);
            return decompressGzip(response.body());

        } catch (Exception e) {
            throw new Exception(); 
        } catch (AssertionError a) {
            System.out.println("Transactions GET fail : " + response);
            throw new Exception(); 
        }
    }

    /*
     * Transactions are returned Gzip encoded so must be decompressed first
     */
    private static String decompressGzip(byte[] compressed) throws Exception {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
        InputStreamReader reader = new InputStreamReader(gis, java.nio.charset.StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(reader)) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }


}