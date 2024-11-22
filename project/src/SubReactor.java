import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.logging.Logger;


public class SubReactor implements Runnable{

    private final Selector selector;

    Logger logger = Logger.getLogger(SubReactor.class.getName());

    public SubReactor(Selector selector) {
        this.selector = selector;
    }
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> keySets = selector.selectedKeys();
                keySets.forEach(e -> dispatch(e));
                keySets.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey selectKey) {
        Runnable handler = (Runnable) selectKey.attachment();
        if (handler != null) {
            handler.run();
        } else {
            System.out.println("<error> dispatch handler error!");
        }
    }
}
