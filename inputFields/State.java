package main.java.ch.fhnw.ws4c.retroMetro.inputFields;

import javafx.scene.paint.Color;

/**
 * Created by Lukas W on 07.06.2016.
 */
public enum State {
    NEUTRAL (5, Color.DODGERBLUE),
    VALID   (3, Color.SPRINGGREEN),
    UNVALID (2, Color.INDIANRED),
    COMPROMISED (1, Color.ORANGE),
    LOCKED (0, Color.LIGHTGRAY),
    UNSELECTED (4, Color.DARKGRAY)
    ;

    // Enum class API
    public final int CODE;
    private Color color;

    State(int code, Color color) {
        this.CODE = code;
        this.color = color;
    }

    public void setColor() { this.color = color; }

    public Color getColor() { return this.color; }

    public static State getStateByCode(int code) {
        State state = null;
        switch (code) {
            case 0:
                state = LOCKED;
                break;
            case 1:
                state = COMPROMISED;
                break;
            case 2:
                state = UNVALID;
                break;
            case 3:
                state = VALID;
                break;
            case 4:
                state = UNSELECTED;
                break;
            case 5:
                state = NEUTRAL;
                break;
            default: break;
        }
        return state;
    }
}
