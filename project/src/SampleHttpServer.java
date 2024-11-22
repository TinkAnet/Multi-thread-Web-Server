import http.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;

/**
 * a sample http server
 */
public class SampleHttpServer {

    //server socket
    private ServerSocketChannel serverSocket;

    //hold selectors

    private Selector acceptSelector;
    private Selector reqSelector;
    private SubReactor acceptReactor;
    private SubReactor reqReactor;

    //private AtomicInteger next = new AtomicInteger(0);

    //define server listen port

    private Logger logger;

    public SampleHttpServer() throws IOException {
        //init server socket
        serverSocket = ServerSocketChannel.open();
        InetSocketAddress servAddr = new InetSocketAddress("0.0.0.0", ServerConfig.LISTEN_PORT);
        serverSocket.socket().bind(servAddr);
        //set socket to no blocking
        serverSocket.configureBlocking(false);
        //init selectors
        acceptSelector = Selector.open();//for accept event
        reqSelector = Selector.open();//for read event
        //register serverSocker to selectors[0] with accept event
        SelectionKey acceptKey = serverSocket.register(acceptSelector, SelectionKey.OP_ACCEPT);
        //attach AcceptHandler
        acceptKey.attach(new AcceptHandler(serverSocket, reqSelector));
        //init sub reactors
        acceptReactor = new SubReactor(acceptSelector);
        reqReactor = new SubReactor(reqSelector);
        logger = Logger.getLogger(SampleHttpServer.class.getName());
    }

    //start server
    public void start() {
        new Thread(acceptReactor).start();
        new Thread(reqReactor).start();
        logger.info("http Server is listening ..");
    }



}
