import http.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    final SocketChannel channel;
    final SelectionKey selectKey;

    final ByteBuffer byteBuffer = ByteBuffer.allocate(2048 * 1024);

    static ExecutorService pool = Executors.newFixedThreadPool(8);

    private Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(SelectionKey selectKey, SocketChannel channel) {
        this.selectKey = selectKey;
        this.channel = channel;
    }

    public void run() {
        pool.submit(() -> asyncRun());
    }

    public synchronized void asyncRun() {
        try {
            if (selectKey.interestOps() == SelectionKey.OP_READ) {
                int length = 0;
                int sumLen = 0;
                while ((length = channel.read(byteBuffer)) > 0) {
                    sumLen += length;
                }
                if (sumLen > 0) {
                    HttpRequestMessage httpReqMessage = new HttpRequestMessage();
                    int ret = httpReqMessage.parse(new String(byteBuffer.array(), 0, sumLen));
                    HttpResponseMessage response;
                    if (ret != 0) {
                        response = HttpResponseMessage.fastBuild(400, "bad request", "400 Bad Request");
                    } else {
                        response = dealHttpMessage(httpReqMessage);
                    }
                    recordAccess(httpReqMessage, response);
                    byteBuffer.clear();
                    logger.info("response: " + response.toBytes());
                    byteBuffer.put(response.toBytes());
                    //flip buffer to read mode
                    byteBuffer.flip();
                    selectKey.interestOps(SelectionKey.OP_WRITE);
                }
            } else {
                channel.write(byteBuffer);
                byteBuffer.clear();
                selectKey.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            logger.info("client is disconnected!");
            selectKey.cancel();
        }
    }

    private HttpResponseMessage dealHttpMessage(HttpRequestMessage httpReqMessage) {
        String urlPath = httpReqMessage.getUrlPath();
        Path resourcePath = Paths.get(ServerConfig.ROOT_DIR, urlPath);
        File file = resourcePath.toFile();
        if (!file.exists() || !file.isFile()) {
            logger.warning(String.format("file:[%s] not exist", resourcePath.toString()));
            return HttpResponseMessage.fastBuild(404, "not found", "404 Not found");
        } else {
            //read file and return file content
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String lastModify = sdf.format(file.lastModified());
                if (httpReqMessage.getHeaders().containsKey("If-Modified-Since")){
                    if (httpReqMessage.getHeaders().get("If-Modified-Since").equals(lastModify)) {
                        //return 304
                        return HttpResponseMessage.fastBuild(304, "not modified", "304 Not Modified");
                    }
                }
                try {
                    //try to read as text file
                    String body = Files.readString(resourcePath);
                    return HttpResponseMessage.builder().code(200)
                            .status("OK")
                            .body(body.getBytes())
                            .headers("Date", new Date().toString())
                            .headers("Server", "sample http serve")
                            .headers("Last-Modified", lastModify)
                            .headers("Content-type", "text/html;charset=utf8")
                            .headers("Content-length", String.valueOf(body.length()))
                            .build();
                } catch (IOException e) {
                    //read as text file failed, deal as image file
                    FileInputStream inputStream = new FileInputStream(resourcePath.toString());
                    byte[] out = new byte[inputStream.available()];
                    logger.info(" image length: " + out.length);
                    inputStream.read(out);
                    String encodeStr = new String(Base64.getEncoder().encode(out));
                    encodeStr = "data:image/jpg"+";base64,"+encodeStr;
                    logger.info("image base64: " + encodeStr);
                    return HttpResponseMessage.builder().code(200)
                            .status("OK")
                            .body(out)
                            .headers("Date", new Date().toString())
                            .headers("Server", "sample http serve")
                            .headers("Last-Modified", lastModify)
                            .headers("Content-type", "image")
                            .headers("Content-length", String.valueOf(out.length))
                            .build();
                }
            } catch (IOException e) {
                logger.warning(String.format("read file:[%s]  failed!", resourcePath.toString()));
                e.printStackTrace();
                return HttpResponseMessage.fastBuild(404, "not found", "404 Not found");
            }
        }
    }

    private void recordAccess(HttpRequestMessage request, HttpResponseMessage response) {
        try {
            logger.info("writing access log ...");
            AccLogItem accLogItem = new AccLogItem();
            accLogItem.setClientAddr(channel.getRemoteAddress().toString());
            accLogItem.setReqTime(new Date());
            accLogItem.setPath(request.getUrlPath());
            accLogItem.setRetCode(response.getCode());
            AccLogRecorder.record(accLogItem);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
