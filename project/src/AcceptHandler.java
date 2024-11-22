import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class AcceptHandler implements Runnable{

    private ServerSocketChannel serverSocket;

    private Selector reqSelector;

    private Logger logger;

    public AcceptHandler(ServerSocketChannel serverSocket, Selector selector) {
        this.serverSocket = serverSocket;
        this.reqSelector = selector;
        logger = Logger.getLogger(AcceptHandler.class.getName());
    }
    public void run() {
        try{
            SocketChannel channel = serverSocket.accept();
            if (channel != null) {
                logger.info("new client connected.");
                channel.configureBlocking(false);
                SelectionKey selectKey = channel.register(reqSelector, 0);
                selectKey.attach(new RequestHandler(selectKey, channel));
                selectKey.interestOps(SelectionKey.OP_READ);
                reqSelector.wakeup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
