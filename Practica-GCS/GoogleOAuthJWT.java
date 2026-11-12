import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GoogleOAuthJWT {

    public static void main(String[] args) throws Exception {

        // 1. Leer JSON credentials
        if (args.length != 4) {
            System.err.println(
                    "Uso: GoogleOAuthJWT <credentials.json> <bucket> <objeto> <archivo-local>");
            System.exit(1);
        }
        String json = Files.readString(Paths.get(args[0]), StandardCharsets.UTF_8);

        JsonObject creds = JsonParser.parseString(json).getAsJsonObject();

        String clientEmail = creds.get("client_email").getAsString();
        String privateKeyPem = creds.get("private_key").getAsString();
        String tokenUri = creds.get("token_uri").getAsString();

        // 2. Limpiar clave privada PEM
        privateKeyPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        // 3. Crear Header JWT
        String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

        // 4. Crear Payload JWT
        long now = Instant.now().getEpochSecond();

        String payloadJson = "{"
                + "\"iss\":\"" + clientEmail + "\","
                + "\"scope\":\"https://www.googleapis.com/auth/devstorage.read_only\","
                + "\"aud\":\"" + tokenUri + "\","
                + "\"exp\":" + (now + 3600) + ","
                + "\"iat\":" + now
                + "}";

        String payload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        // 5. Firmar JWT
        String unsignedToken = header + "." + payload;

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(unsignedToken.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();

        String jwt = unsignedToken + "." + base64UrlEncode(signed);

        // 6. Enviar a OAuth token endpoint
        String requestBody = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer"
                + "&assertion=" + jwt;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 7. Extraer el token de acceso
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Error al solicitar el token OAuth. HTTP "
                    + response.statusCode());
        }
        String accessToken = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .get("access_token")
                .getAsString();

        // 8. Descargar el contenido del objeto
        String encodedObject = URLEncoder.encode(args[2], StandardCharsets.UTF_8)
                .replace("+", "%20");
        URI downloadUri = URI.create("https://storage.googleapis.com/storage/v1/b/"
                + args[1] + "/o/" + encodedObject + "?alt=media");

        HttpRequest downloadRequest = HttpRequest.newBuilder(downloadUri)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<byte[]> download = client.send(
                downloadRequest, HttpResponse.BodyHandlers.ofByteArray());

        if (download.statusCode() != 200) {
            throw new IllegalStateException("Error al descargar el objeto. HTTP "
                    + download.statusCode());
        }

        // 9. Guardar el archivo y mostrar el resultado
        Files.write(Paths.get(args[3]), download.body(), StandardOpenOption.CREATE_NEW);
        System.out.printf("Descarga completada: %s (%d bytes)%n",
                args[3], download.body().length);
    }

    private static String base64UrlEncode(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }
}
