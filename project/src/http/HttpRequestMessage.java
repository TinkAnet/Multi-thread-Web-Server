package http;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * define http request message
 */
public class HttpRequestMessage {
    //just define common methods
    /*public final static short METHOD_GET = 0;
    public final static short METHOD_POST = 1;
    public final static short METHOD_PUT = 2;
    public final static short METHOD_DELETE = 3;*/

    private final String HEADER_BODY_SPLIT = "\r\n\r\n";
    private final String LINE_SPLIT = "\r\n";

    private String method;
    private String urlPath;
    private String version;
    private Map<String, String> headers;
    private String body;

    private Logger logger;

    public HttpRequestMessage() {
        headers = new HashMap<>();
        logger = Logger.getLogger(HttpRequestMessage.class.getName());
    }
    /**
     * parse string message to http message
     * @param msg
     * @return 0:success, other: failed
     */
    public int parse(String msg) {
        if (msg == null || msg.isEmpty() || msg.isBlank()) {
            //logger.warning("request msg is empty or blank, parse message failed!");
            return 1;
        }
        logger.info("begin to parse http message ...");
        String msgSpt[] = msg.split(HEADER_BODY_SPLIT);
        String headerLines[] = msgSpt[0].split(LINE_SPLIT);
        if (headerLines.length < 1) {
            logger.warning("error headers!");
            return 2;
        }
        //get method, path, and version from first line
        String httpInfo[] = headerLines[0].split(" ");
        if (httpInfo.length != 3) {
            logger.warning("error http info!");
            return 3;
        }
        this.method = httpInfo[0];
        this.urlPath = httpInfo[1];
        this.version = httpInfo[2];
        //parse headers
        for (int i = 1; i < headerLines.length; i++) {
            String lineSpt[] = headerLines[i].split(": ");
            if (lineSpt.length != 2) {
                logger.warning("error headers!");
                return 2;
            }
            //header with lower case
            this.headers.put(lineSpt[0].trim().toLowerCase(), lineSpt[1]);
        }
        //parse body
        if (msgSpt.length ==2) {
            this.body = msgSpt[2];
        }
        logger.info("parse http message success! msg: " + this.toString());
        return 0;
    }

    public String getMethod() {
        return method;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
