package http;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseMessage {

    private String httpVersion;

    private Integer code;

    private String status;
    private Map<String, String> headers = new HashMap<>();

    private byte[] body;

    private HttpResponseMessage(Builder builder) {
        this.httpVersion = builder.httpVersion;
        this.code = builder.code;
        this.status = builder.status;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public Integer getCode() {
        return code;
    }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        //status line
        sb.append(httpVersion)
                .append(" ")
                .append(code)
                .append(" ")
                .append(status)
                .append("\r\n");
        //add headers
        headers.entrySet().forEach(entry -> {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        });
        sb.append("\r\n");
        if (body.length > 0) {
            byte[] headerBytes = sb.toString().getBytes();
            byte[] result = new byte[headerBytes.length + body.length];
            System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
            System.arraycopy(body, 0, result,  headerBytes.length, body.length);
            return result;
        } else {
            return sb.toString().getBytes();
        }
        /*String rsp =  "HTTP/1.1 200 OK \r\n";
        rsp += "Date: Sat 31 Dec 2020 12:12:12 GMT\r\n";
        rsp += "Server: sample http server\r\n";
        rsp += "Content-type: text/html;charset=utf8\r\n";
        rsp += "Content-length: 14\r\n\r\n";
        rsp += "Hello world!\r\n";*/

    }

    public static Builder builder() {
        return new Builder();
    }

    public static HttpResponseMessage fastBuild(Integer code, String status, String msg) {
        return new Builder().code(code)
                .status(status)
                .body(msg.getBytes())
                .headers("Date", new Date().toString())
                .headers("Server", "sample http serve")
                .headers("Content-type", "text/html;charset=utf8")
                .headers("Content-length", String.valueOf(msg.length()))
                .build();
    }

    public static class Builder {
        private String httpVersion;
        private Integer code;
        private String status;
        private Map<String, String> headers = new HashMap<>();
        private byte[] body;

        public Builder() {
            //default http version
            httpVersion = "HTTP/1.1";
            body = new byte[0];
        }

        public Builder httpVersion(String httpVersion) {
            this.httpVersion = httpVersion;
            return this;
        }

        public Builder code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder headers(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public HttpResponseMessage build() {
            return new HttpResponseMessage(this);
        }
    }
}
