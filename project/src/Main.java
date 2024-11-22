import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            SampleHttpServer httpServer = new SampleHttpServer();
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}