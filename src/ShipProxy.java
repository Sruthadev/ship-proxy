import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ShipProxy {

    private static final int SHIP_PROXY_PORT = 8080;
    private static final String OFFSHORE_HOST = "localhost";
    private static final int OFFSHORE_PROXY_PORT = 9090;

    private static Socket offshoreSocket;
    private static OutputStream offshoreOut;
    private static InputStream offshoreIn;

    private static BlockingQueue<Socket> requestQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {

        System.out.println("Ship proxy starting on port: " + SHIP_PROXY_PORT);

        ServerSocket proxySocket = new ServerSocket(SHIP_PROXY_PORT);

        offshoreSocket = new Socket(OFFSHORE_HOST, OFFSHORE_PROXY_PORT);
        offshoreOut = offshoreSocket.getOutputStream();
        offshoreIn = offshoreSocket.getInputStream();

        new Thread(() -> {
            while (true){
                try {
                    Socket browserSocket = requestQueue.take();
                    handleRequest(browserSocket);
                } catch (Exception e){
                    System.out.println("Error in request handler: " + e.getMessage());
                }
            }
        }).start();

        while (true){
            Socket browserSocket = proxySocket.accept();
            requestQueue.add(browserSocket);
        }
    }

    private static void handleRequest(Socket browserSocket){
        try (
                InputStream browserIn = browserSocket.getInputStream();
                OutputStream browserOut = browserSocket.getOutputStream();
                ){
                    ByteArrayOutputStream requestBuffer = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int read;
                    browserSocket.setSoTimeout(1000);

                    while ((read = browserIn.read(buffer)) != -1){
                        requestBuffer.write(buffer, 0, read);
                        if(read < buffer.length) break;
                    }

                    byte[] requestBytes = requestBuffer.toByteArray();

                    sendFramed(offshoreOut, requestBytes);

                    byte[] responseBytes = receivedFramed(offshoreIn);

                    browserOut.write(responseBytes);
                    browserOut.flush();

                    browserSocket.close();

        }catch (Exception e){
            System.out.printf("Error in browser request handling: " + e.getMessage());
        }
    }

    public static void sendFramed(OutputStream offshoreOut, byte[] requestBytes) throws IOException {
        DataOutputStream dos = new DataOutputStream(offshoreOut);
        dos.write(requestBytes.length);
        dos.write(requestBytes);
        dos.flush();
    }

    public static byte[] receivedFramed(InputStream offshoreIn) throws IOException {
        DataInputStream dis = new DataInputStream(offshoreIn);
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return data;
    }
}