import java.io.InputStream;
import java.io.OutputStream;
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

    public static void main(String[] args) {

        System.out.println("Ship proxy starting on port: " + SHIP_PROXY_PORT);

    }
}