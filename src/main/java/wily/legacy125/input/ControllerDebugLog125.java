package wily.legacy125.input;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Small append-only diagnostic log for controller issues outside Prism's visible console. */
public final class ControllerDebugLog125 {
    private static final File FILE = new File("legacy4j-controller.log");

    private ControllerDebugLog125() {
    }

    public static synchronized void log(String message) {
        String line = "[Legacy4J/Input " + System.currentTimeMillis() + "] " + message;
        System.out.println(line);
        try {
            FileWriter writer = new FileWriter(FILE, true);
            try {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            } finally {
                writer.close();
            }
        } catch (IOException ignored) {
        }
    }
}
