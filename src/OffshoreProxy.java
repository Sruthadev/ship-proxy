import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OffshoreProxy {

    private static final int LISTEN_PORT = 9090;

    public static void main(String[] args) {
        System.out.println("Offshore proxy listening on port: " + LISTEN_PORT);

        try(ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)){
            Socket shipSocket = serverSocket.accept();
            System.out.println("Connected to ship proxy");

            InputStream in = shipSocket.getInputStream();
            OutputStream out = shipSocket.getOutputStream();
            DataInputStream dis = new DataInputStream(in);
            DataOutputStream dos = new DataOutputStream(out);

            while (true){
                try{
                    int requestLength = dis.readInt();
                    byte[] requestBytes = new byte[requestLength];
                    dis.readFully(requestBytes);

                    String rawHttpRequest = new String(requestBytes, StandardCharsets.UTF_8);

                    String[] lines = rawHttpRequest.split("\r\n");
                    String requestLine = lines[0];
                    String[] parts = requestLine.split(" ");
                    String method = parts[0];
                    String fullUrl = parts[1];
                    System.out.println("\nReceived request:\n" + fullUrl);

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                            .uri(URI.create(fullUrl))
                            .method(method, HttpRequest.BodyPublishers.noBody())
                            .build();

                    HttpResponse<byte[]> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

                    String statusLine = "HTTP/1.1 " + httpResponse.statusCode() + " OK\r\n";
                    String headers = "Content-Length: " + httpResponse.body().length + "\r\n\r\n";
                    byte[] responseBody = httpResponse.body();

                    ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                    responseBuffer.write(statusLine.getBytes(StandardCharsets.UTF_8));
                    responseBuffer.write(headers.getBytes(StandardCharsets.UTF_8));
                    responseBuffer.write(responseBody);

                    byte[] fullResponseBytes = responseBuffer.toByteArray();

                    dos.writeInt(fullResponseBytes.length);
                    dos.write(fullResponseBytes);
                    dos.flush();

                    System.out.println("Response sent to ship.");

                } catch (Exception e) {
                    System.out.println("Error in request handling: " +  e.getMessage());
                    break;
                }
            }

            shipSocket.close();

        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
