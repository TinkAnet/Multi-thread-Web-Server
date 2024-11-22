package http;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AccLogRecorder {

    private static BufferedWriter writer = null;

    public static synchronized void record(AccLogItem logItem) {
        if (writer == null) {
            try {
                FileWriter fileWriter = new FileWriter(ServerConfig.ACC_LOG, true);
                writer = new BufferedWriter(fileWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            writer.write(String.format("%s %s %s %d", logItem.getClientAddr(), logItem.getReqTime().toString()
                    , logItem.getPath(), logItem.getRetCode()));
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
