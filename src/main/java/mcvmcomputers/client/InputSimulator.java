package mcvmcomputers.client;

import mcvmcomputers.client.utils.QemuKey;
import mcvmcomputers.client.utils.QmpClient;

public class InputSimulator {
    public static void main(String[] args) {
        try {
            QmpClient client = new QmpClient("localhost", 4444);

            // Simulate pressing and releasing the 'A' key
            client.sendKey(QemuKey.keyboard(0x41, true));  // Press 'A'
            client.sendKey(QemuKey.keyboard(0x41, false)); // Release 'A'

            // Simulate mouse movement
            client.sendKey(QemuKey.mouseDelta(10, 5, 0, false)); // Move mouse

            // Simulate left click
            client.sendKey(QemuKey.leftClick(true));  // Press left button
            client.sendKey(QemuKey.leftClick(false)); // Release left button

            // Simulate scroll
            client.sendKey(QemuKey.scrollUp());
            client.sendKey(QemuKey.scrollDown());

            client.close();
        } catch (Exception e) {
            System.err.println("Input simulation failed: " + e.getMessage());
        }
    }
}