package mcvmcomputers.client.utils;

import java.io.*;
import java.net.Socket;

public class QmpClient {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public QmpClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(5000); // Optional timeout
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        initialize();
    }

    private void initialize() throws IOException {
        String greeting = reader.readLine();
        System.out.println("[QMP] Greeting: " + greeting);

        sendRaw("{\"execute\": \"qmp_capabilities\"}");
        String response = reader.readLine();
        System.out.println("[QMP] Capabilities: " + response);
    }

    public void sendKey(QemuKey key) throws IOException {
        String json;

        switch (key.type) {
            case KEYBOARD:
                json = String.format(
                    "{ \"execute\": \"input-send-event\", \"arguments\": { \"events\": [ { \"type\": \"key\", \"data\": { \"key\": { \"down\": %b, \"keysym\": \"0x%x\" } } } ] } }",
                    key.pressed, key.keySym
                );
                break;

            case MOUSE:
                json = String.format(
                    "{ \"execute\": \"input-send-event\", \"arguments\": { \"events\": [ " +
                    "{ \"type\": \"rel\", \"data\": { \"axis\": \"x\", \"value\": %d } }, " +
                    "{ \"type\": \"rel\", \"data\": { \"axis\": \"y\", \"value\": %d } }, " +
                    "{ \"type\": \"btn\", \"data\": { \"button\": \"%s\", \"down\": %b } } " +
                    "] } }",
                    key.deltaX, key.deltaY,
                    getMouseButtonName(key.buttonMask),
                    key.mousePressed
                );
                break;

            case SCROLL:
                json = String.format(
                    "{ \"execute\": \"input-send-event\", \"arguments\": { \"events\": [ " +
                    "{ \"type\": \"rel\", \"data\": { \"axis\": \"wheel\", \"value\": %d } } " +
                    "] } }",
                    key.scrollDirection
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown QemuKey type");
        }

        sendRaw(json);
        System.out.println("[QMP] Sent: " + json);

        String response = reader.readLine();
        if (response != null) {
            System.out.println("[QMP] Response: " + response);
        } else {
            System.err.println("[QMP] No response received.");
        }
    }

    private void sendRaw(String json) throws IOException {
        writer.write(json);
        writer.write("\n");
        writer.flush();
    }

    private String getMouseButtonName(int mask) {
        if ((mask & QemuKey.BUTTON_LEFT) != 0) return "left";
        if ((mask & QemuKey.BUTTON_RIGHT) != 0) return "right";
        if ((mask & QemuKey.BUTTON_MIDDLE) != 0) return "middle";
        return "unknown";
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }
}