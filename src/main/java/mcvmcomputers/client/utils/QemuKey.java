package mcvmcomputers.client.utils;

public class QemuKey {
    public enum Type {
        KEYBOARD, MOUSE, SCROLL
    }

    public static final int BUTTON_LEFT = 1;
    public static final int BUTTON_RIGHT = 2;
    public static final int BUTTON_MIDDLE = 4;

    public Type type;

    // Keyboard
    public int keySym;
    public boolean pressed;

    // Mouse
    public int deltaX;
    public int deltaY;
    public int buttonMask;
    public boolean mousePressed;

    // Scroll
    public int scrollDirection;

    // Constructors
    private QemuKey(int keySym, boolean pressed) {
        this.type = Type.KEYBOARD;
        this.keySym = keySym;
        this.pressed = pressed;
    }

    private QemuKey(int deltaX, int deltaY, int buttonMask, boolean mousePressed) {
        this.type = Type.MOUSE;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.buttonMask = buttonMask;
        this.mousePressed = mousePressed;
    }

    private QemuKey(int scrollDirection) {
        this.type = Type.SCROLL;
        this.scrollDirection = scrollDirection;
    }

    // Static factory methods
    public static QemuKey keyboard(int keySym, boolean pressed) {
        return new QemuKey(keySym, pressed);
    }

    public static QemuKey mouseDelta(int dx, int dy, int buttonMask, boolean pressed) {
        return new QemuKey(dx, dy, buttonMask, pressed);
    }

    public static QemuKey leftClick(boolean pressed) {
        return new QemuKey(0, 0, BUTTON_LEFT, pressed);
    }

    public static QemuKey rightClick(boolean pressed) {
        return new QemuKey(0, 0, BUTTON_RIGHT, pressed);
    }

    public static QemuKey middleClick(boolean pressed) {
        return new QemuKey(0, 0, BUTTON_MIDDLE, pressed);
    }

    public static QemuKey scrollUp() {
        return new QemuKey(+1);
    }

    public static QemuKey scrollDown() {
        return new QemuKey(-1);
    }

    @Override
    public String toString() {
        switch (type) {
            case KEYBOARD:
                return "Key[" + Integer.toHexString(keySym) + "] " + (pressed ? "down" : "up");
            case MOUSE:
                return "Mouse[dx=" + deltaX + ", dy=" + deltaY + ", button=" + buttonMask + ", " + (mousePressed ? "down" : "up") + "]";
            case SCROLL:
                return "Scroll[" + scrollDirection + "]";
            default:
                return "Unknown";
        }
    }
}