package main.java.ch.fhnw.ws4c.retroMetro.indicators;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import main.java.ch.fhnw.ws4c.retroMetro.*;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

/**
 * Created by Lukas W on 24.03.2016.
 */
public class Led extends Region implements ViewTemplate{

    Region frame;
        double frame_SizeXY_Factor = 1.02;
    Circle highlight;
        double highlight_Rad_Factor = 0.33428;
    Circle ledlight;
        double ledlight_Rad_Factor = 0.41428;
    Circle backplate;
        double backplate_Rad_Factor = 0.45714;

    private Stop stop1 = new Stop(0, new Color(
            0.98431372549019607843137254901961,
            0.41568627450980392156862745098039,
            0.41568627450980392156862745098039,
            1.0));
    private Stop stop2 = new Stop(0.36, new Color(
            0.87843137254901960784313725490196,
            0.36470588235294117647058823529412,
            0.29803921568627450980392156862745,
            1.0));
    private Stop stop3 = new Stop(1, new Color(
            0.8,
            0.21960784313725490196078431372549,
            0.21960784313725490196078431372549,
            1.0));


    // *** LOGICAL BEHAVIOUR VARIABLES ***
    private boolean isToggeable = false;
    private boolean toggleEnablesBlink = false;

    // *** LOGICAL BEHAVIOUR PROPERTIES ***
    private BooleanProperty toggledProperty = new SimpleBooleanProperty();
    /** blink rate in seconds, when changed it will enable blinking */
    private DoubleProperty blinkRateProperty = new SimpleDoubleProperty();
    private BooleanProperty blinkEnabledProperty = new SimpleBooleanProperty(false);

    // *** EVENTS LISTENERS ANIMATIONHANDLERS ***
    private AnimationTimer timer = new AnimationTimer() {
        private long lastTimerCall;
        @Override
        public void handle(long now) {
            if (now > lastTimerCall + getBlinkRate()) {
                setState(!isToggled());
                lastTimerCall = now;
            }
        }
    };
    // TODO EASE ANIMATION

    // *** VISUAL BEHAVIOUR VARIABLES ***
    //Tolerance before resizing on this.heightProperty -> when using window side snapping on windows
    byte tolerance = 1;

    // *** CONSTRUCTORS ***
    public Led(boolean toggled) {
        String stylesheet = this.getClass().getResource("IndicatorStyle.css").toExternalForm();
        this.getStylesheets().add(stylesheet);
        this.toggledProperty.setValue(toggled);
        initSequence();
        setRGB(new Color(0.98431372549019607843137254901961,
                0.51568627450980392156862745098039,
                0.51568627450980392156862745098039,
                1.0));
    }
    public Led(boolean toggled, Color color) { this(toggled); setRGB(color); }

    // *** PUBLIC API ***
    public void setRGB(Color c) {
        // HueShift, Saturation, Brightness, Opacity
        Color c1 = c.deriveColor(0, 1.0, 0.99999, 1.0);
        Color c2 = c.deriveColor(-5, 1.8, 0.8, 1.0);
        Color c3 = c.deriveColor(0, 1.5, 0.64, 1.0);
        stop1 = new Stop(0, c1);
        stop2 = new Stop(0.36, c2);
        stop3 = new Stop(1.0, c3);
        RadialGradient rad = new RadialGradient(90, 0, 0.5, 0.5, 0.5, true, CycleMethod.REFLECT, stop1, stop2, stop3);
        ledlight.setFill(rad);
    }

    public void on() { setState(true); }
    public void off() { setState(false); }

    public void setState(boolean toggled) {
        toggledProperty.setValue(toggled);
    }

    public void setIsClickable(boolean isToggeable) { this.isToggeable = isToggeable; }
    public void setClickableEnablesBlinking(boolean toggleEnablesBlink) { this.toggleEnablesBlink = toggleEnablesBlink; }

    /**
     * set the blink rate
     * nano second = 1/1_000_000_000 second
     * @param blinkRate blink rate in NANO seconds
     */
    public void setBlinkRate(double blinkRate) {
        this.blinkRateProperty.set(blinkRate);
    }

    public void setBlinkEnabled(boolean enabled) { this.blinkEnabledProperty.set(enabled); }

    // *** PUBLIC GETTER ***
    public boolean isToggled() { return toggledProperty.get(); }
    public boolean isClickable() { return this.isToggeable; }
    public double getBlinkRate() { return this.blinkRateProperty.get(); }
    public boolean isBlinkEnabled() { return blinkEnabledProperty.get(); }

    // *** INITIALIZER SEQUENCE ***
    @Override
    public void initializeControls() {
        frame = new Region();
        highlight = new Circle();
        ledlight = new Circle();
        backplate = new Circle();
    }

    @Override
    public void initializeLayout() {
	    this.getChildren().addAll(backplate, ledlight, highlight, frame);
    }

    @Override
    public void layoutPanes() {

    }

    @Override
    public void layoutControls() {
        this.autosize();
    }

    @Override
    public void addListeners() {
        // *** LOGIC CONTROLLING LISTENERS ***
        this.toggledProperty.addListener(((observable, oldValue, newValue) -> {
            // TODO: change when animated
            ledlight.setVisible(newValue);
        }));
        // starts or stops the animation timer
        this.blinkEnabledProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) { timer.start();
            } else { timer.stop(); }
        });
        // enable blinking when rate changes and nor already enabled
        this.blinkRateProperty.addListener(observable -> { if(!this.isBlinkEnabled()) this.blinkEnabledProperty.set(true); });

        // *** VISUAL CONTROLLING LISTENERS ***
        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            if ((newValue.intValue()>oldValue.intValue()+tolerance || newValue.intValue()+tolerance<oldValue.intValue()) && newValue.doubleValue()>0.0) resize(false);
        });
        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            if ((newValue.intValue()>oldValue.intValue()+tolerance || newValue.intValue()+tolerance<oldValue.intValue()) && newValue.doubleValue()>0.0) resize(false);
        });
    }

    @Override
    public void addBindings() {

    }

    @Override
    public void addEvents() {
        this.setOnMouseClicked(event -> {
            if(toggleEnablesBlink) {
                this.blinkEnabledProperty.set(!isBlinkEnabled());
                if (!isBlinkEnabled()) this.setState(false);
            }
            else
                if (isToggeable) { this.setState(!toggledProperty.getValue()); }
        });
    }

    @Override
    public void applyStylesheet() {
        this.setId("led-region");
        frame.setId("led-frame");
        highlight.setId("led-highlight");
        ledlight.setId("led-light");
        backplate.setId("led-backplate");
    }

    @Override
    public void applyJavaFXStyles() {

    }

    // *** RESIZING ***
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        resize(true);
    }

    private void resize(boolean resizeAll) {
        double width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (width > 0 && height > 0) {
            double radius = (width>=height) ? height : width;
            double centerX = width/2;
            double centerY = height/2;

            frame.setPrefSize(radius*frame_SizeXY_Factor, radius*frame_SizeXY_Factor);
            frame.setMinSize(radius*frame_SizeXY_Factor, radius*frame_SizeXY_Factor);
            frame.setMaxSize(radius*frame_SizeXY_Factor, radius*frame_SizeXY_Factor);

            frame.setLayoutX(centerX-(radius*frame_SizeXY_Factor)/2);
            frame.setLayoutY(centerY-(radius*frame_SizeXY_Factor)/2);

            if (resizeAll) { // avoid unnecessary recalculation
                highlight.setRadius(radius*highlight_Rad_Factor);
                highlight.setCenterY(centerY);
                highlight.setCenterX(centerX);
                ledlight.setRadius(radius*ledlight_Rad_Factor);
                ledlight.setCenterX(centerX);
                ledlight.setCenterY(centerY);
                backplate.setRadius(radius*backplate_Rad_Factor);
                backplate.setCenterY(centerY);
                backplate.setCenterX(centerX);
            }
        }
    }
}
