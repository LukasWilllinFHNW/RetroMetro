package main.java.ch.fhnw.ws4c.retroMetro.inputFields;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import main.java.ch.fhnw.ws4c.retroMetro.ViewTemplate;
import main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers.InputVerifierInterface;

import java.lang.reflect.Type;

/**
 * Created by Lukas W on 06.06.2016.
 */
public class TrainInputField extends Region implements ViewTemplate, InputVerifierInterface{

    private InputVerifierInterface inputVerifier;

    // *** REGIONS, PANES & CONTROLS ***
    private HBox hbox;
    private Pane inputPane;
    private TextField inputField;
    private Region trainSymbol;
    private Region exclamationMark;
    private Region lockSymbol;
    private Rectangle underLine;
    private Rectangle underLineGhost;

    // *** LOGICAL BEHAVIOUR VARIABLES ***

    // *** LOGICAL BEHAVIOUR PROPERTIES ***
    private final IntegerProperty stateCODEProperty = new SimpleIntegerProperty();
    private final BooleanProperty trainVisibleProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty lockVisibleProperty = new SimpleBooleanProperty(false);

    // *** EVENTS, LISTENERS & ANIMATION HANDLERS ***
    private TranslateTransition translateTransition;
    private ScaleTransition scaleTransitionGhostLift;
    private FadeTransition fadeTransitionGhostLift;
    private ParallelTransition parallelTransitionGhostLift;
    private FadeTransition fadeInTransitionExclMark;
    private FadeTransition fadeOutTransitionExclMark;
    private FadeTransition fadeInTransitionLock;
    private FadeTransition fadeOutTransitionLock;
    private FadeTransition fadeInTransitionCoverUp;
    private ScaleTransition scaleTransitionCoverUp;
    private ParallelTransition parallelTransitionCoverUp;
    private FadeTransition fadeOutTransitionUnCover;
    private ScaleTransition scaleTransitionUnCover;
    private ParallelTransition parallelTransitionUnCover;

    // *** VISUAL BEHAVIOUR VARIABLES ***
    public static final double MIN_WIDTH = 5;
    public static final double MIN_HEIGHT = 5;
    public static final double EXCL_MARK_WIDTH_FACTOR = 0.17;
    public static final double TRAIN_SYMBOL_WIDTH_FACTOR = 0.8;
    public static final double LOCK_SYMBOL_WIDTH_FACTOR = 0.5;
    public static final double SPACING_FACTOR = 0.1;
    public static final double FONT_SIZE_FACTOR = 0.7;

    // *** VISUAL BEHAVIOUR PROPERTIES ***
    private final DoubleProperty TRAIN_SYMBOL_WIDTH_FACTOR_PROPERTY = new SimpleDoubleProperty(0.0);
    private final DoubleProperty LOCK_SYMBOL_WIDTH_FACTOR_PROPERTY = new SimpleDoubleProperty(0.0);

    // ----------------------------------------- PUBLIC SECTION ----------------------------------------------
    // *** CONSTRUCTORS ***
    public TrainInputField() {
        String stylesheet = this.getClass().getResource("InputFieldStyle.css").toExternalForm();
        this.getStylesheets().add(stylesheet);
        initSequence();
        this.setState(State.NEUTRAL);
    }
    public TrainInputField(InputVerifierInterface inputVerifier) {
        this();
        this.inputVerifier = inputVerifier;
        this.stateCODEProperty.bind(inputVerifier.getStateCodeProperty());
        this.inputVerifier.setSourceProperty(inputField.textProperty());
    }
    public TrainInputField(InputVerifierInterface inputVerifier, boolean visibleTrain) {
        this(inputVerifier);
        this.trainVisibleProperty.set(visibleTrain);
        //TODO: needed? -> trainSymbol.setVisible(visibleTrain);
    }
    public TrainInputField(State state, boolean visibleTrain) {
        this();
        setState(state);
        this.trainVisibleProperty.set(visibleTrain);
        //TODO: needed? -> trainSymbol.setVisible(visibleTrain);
    }
    public TrainInputField(boolean visibleTrain) {
        this();
        this.trainVisibleProperty.set(visibleTrain);
        //TODO: needed? -> trainSymbol.setVisible(visibleTrain);
    }


    // *** PUBLIC API ***
    public void setState(State state) {
        Background fill = new Background(new BackgroundFill(state.getColor(), null, null));
        trainSymbol.setBackground(fill);
        underLine.setFill(state.getColor());
        underLineGhost.setFill(state.getColor());
        if (state.CODE == State.UNVALID.CODE || state.CODE == State.COMPROMISED.CODE) {
            exclamationMark.setBackground(fill);
        }
        // Color for lock does not change until enum color is changed
        fill = new Background(new BackgroundFill(State.LOCKED.getColor(), null, null));
        lockSymbol.setBackground(fill);

        animateState(state);
    }

    public void setColor(Color c) {
        Background fill = new Background(new BackgroundFill(c, null, null));
        exclamationMark.setBackground(fill);
        trainSymbol.setBackground(fill);
        underLine.setFill(c);
        underLineGhost.setFill(c);

        // Color for lock does not change until enum color is changed
        fill = new Background(new BackgroundFill(State.LOCKED.getColor(), null, null));
        lockSymbol.setBackground(fill);
    }

    /**
     * Convert a color to rgba(red, green, blue, alpha) no transparent color.
     * @param color the color to convert
     * @return string formatted like rgba(255, 255, 255, 1.0)
     */
    public static String convertColorToString(Color color, double alpha) {
        return "rgba("+color.getRed()+", "+color.getGreen()+", "+color.getBlue()+", "+alpha+")";
    }

    public void animateState(State state) {
        stopAnimations();
        switch (state) {
            case UNVALID:   bumpTrain();
                            alert();
                            liftGhost();
                            break;
            case VALID:     break;
            case COMPROMISED:
                            alert();
                            liftGhost();
                            break;
            case NEUTRAL:   break;
            case LOCKED:    lock();
                            coverUp();
                            break;
            case UNSELECTED: break;
            default: //TODO: What is neutral
                break;
        }
        resize(true);
    }

    public void stopAnimations() {
        if (parallelTransitionGhostLift!=null && parallelTransitionGhostLift.getStatus().equals(Animation.Status.RUNNING))
            parallelTransitionGhostLift.stop();
            underLineGhost.setVisible(false);
        if (exclamationMark.isVisible())
            calmAlert();
        if (lockSymbol.isVisible()) {
            underLineGhost.setVisible(false);
            unlock();
        }

        resize(true);
    }

    // --------------------------------- NON PUBLIC SECTION -------------------------------------------------
    // *** PRIVATE API ***
    private void bumpTrain() {
        if (translateTransition == null) {
            translateTransition = new TranslateTransition(Duration.seconds(1), trainSymbol);
            translateTransition.setInterpolator(Interpolator.EASE_BOTH);
            translateTransition.setFromX(0);
            translateTransition.setByX(60);
            translateTransition.setCycleCount(2);
            translateTransition.setAutoReverse(true);
            translateTransition.durationProperty().set(Duration.millis(100));
        }
        if (translateTransition.getStatus().equals(Animation.Status.RUNNING)) {
            translateTransition.stop();
        }
        translateTransition.play();
    }

    private void liftGhost() {
        /*underLineGhost.setVisible(true);
        Duration durationMillis = new Duration(2000);
        if (fadeTransitionGhostLift == null) {
            fadeTransitionGhostLift = new FadeTransition(Duration.seconds(1), underLineGhost);
            fadeTransitionGhostLift.setFromValue(0.4);
            fadeTransitionGhostLift.setToValue(0.006);
            fadeTransitionGhostLift.setInterpolator(Interpolator.EASE_BOTH);
            fadeTransitionGhostLift.setDuration(durationMillis);
        }
        if (scaleTransitionGhostLift == null) {
            scaleTransitionGhostLift = new ScaleTransition(Duration.seconds(1), underLineGhost);
            scaleTransitionGhostLift.setFromY(1.0);
            scaleTransitionGhostLift.setByY(18);
            scaleTransitionGhostLift.setInterpolator(Interpolator.EASE_BOTH);
            scaleTransitionGhostLift.setDuration(durationMillis);
        }
        if (parallelTransitionGhostLift==null) {
            parallelTransitionGhostLift = new ParallelTransition(fadeTransitionGhostLift, scaleTransitionGhostLift);
            parallelTransitionGhostLift.setOnFinished(event -> {
                parallelTransitionGhostLift.play();
                underLineGhost.setVisible(true);
            });
        }
        if (parallelTransitionGhostLift.getStatus().equals(Animation.Status.RUNNING)) {
            parallelTransitionGhostLift.stop();
        }
        parallelTransitionGhostLift.play();*/
    }

    private void coverUp() {
        /*underLineGhost.setVisible(true);
        Duration durationMillis = new Duration(2500);
        if (fadeInTransitionCoverUp == null) {
            fadeInTransitionCoverUp = new FadeTransition(Duration.seconds(1), underLineGhost);
            fadeInTransitionCoverUp.setFromValue(1.0);
            fadeInTransitionCoverUp.setToValue(0.2);
            fadeInTransitionCoverUp.setDuration(durationMillis);
        }
        if (scaleTransitionCoverUp == null) {
            scaleTransitionCoverUp = new ScaleTransition(Duration.seconds(1), underLineGhost);
            scaleTransitionCoverUp.setFromY(1.0);
            scaleTransitionCoverUp.setByY(18);
            scaleTransitionCoverUp.setInterpolator(Interpolator.EASE_BOTH);
            scaleTransitionCoverUp.setDuration(durationMillis);
        }
        if (parallelTransitionCoverUp==null) {
            parallelTransitionCoverUp = new ParallelTransition(fadeInTransitionCoverUp, scaleTransitionCoverUp);
        }
        if (parallelTransitionCoverUp.getStatus().equals(Animation.Status.RUNNING)) {
            parallelTransitionCoverUp.stop();
        }
        parallelTransitionCoverUp.play();*/
    }

    private void unCover() {
        Duration durationMillis = new Duration(500);
        if (fadeOutTransitionUnCover == null) {
            fadeOutTransitionUnCover = new FadeTransition(Duration.seconds(1), underLineGhost);
            fadeOutTransitionUnCover.setFromValue(0.2);
            fadeOutTransitionUnCover.setToValue(1.0);
            fadeOutTransitionUnCover.setDuration(durationMillis);
        }
        if (scaleTransitionUnCover == null) {
            scaleTransitionUnCover = new ScaleTransition(Duration.seconds(1), underLineGhost);
            scaleTransitionUnCover.setFromY(1.0);
            scaleTransitionUnCover.setByY(18);
            scaleTransitionUnCover.setInterpolator(Interpolator.EASE_BOTH);
            scaleTransitionUnCover.setDuration(durationMillis);
        }
        // TODO add translate transition
        if (parallelTransitionUnCover==null) {
            parallelTransitionUnCover = new ParallelTransition(fadeOutTransitionUnCover, scaleTransitionUnCover);
            parallelTransitionUnCover.setOnFinished(event -> underLineGhost.setVisible(false));
        }
        if (parallelTransitionUnCover.getStatus().equals(Animation.Status.RUNNING)) {
            parallelTransitionUnCover.stop();
        }
        parallelTransitionUnCover.play();
    }

    private void lock() {
        lockVisibleProperty.set(true);
        inputField.setDisable(true);
        Duration durationMillis = new Duration(200);
        if (fadeInTransitionLock == null) {
            fadeInTransitionLock = new FadeTransition(Duration.seconds(1), lockSymbol);
            fadeInTransitionLock.setFromValue(0.0);
            fadeInTransitionLock.setToValue(1.0);
            fadeInTransitionLock.setInterpolator(Interpolator.EASE_BOTH);
            fadeInTransitionLock.setDuration(durationMillis);
        }
        if (fadeInTransitionLock.getStatus().equals(Animation.Status.RUNNING)) {
            fadeInTransitionLock.stop();
        }
        fadeInTransitionLock.play();
    }

    private void unlock() {
        Duration durationMillis = new Duration(200);
        if (fadeOutTransitionLock == null) {
            fadeOutTransitionLock = new FadeTransition(Duration.seconds(1), lockSymbol);
            fadeOutTransitionLock.setFromValue(1.0);
            fadeOutTransitionLock.setToValue(0.0);
            fadeOutTransitionLock.setInterpolator(Interpolator.EASE_BOTH);
            fadeOutTransitionLock.setDuration(durationMillis);
            fadeOutTransitionLock.setOnFinished(event -> {
                inputField.setDisable(false);
                lockVisibleProperty.set(false);
            });
        }
        if (fadeOutTransitionLock.getStatus().equals(Animation.Status.RUNNING)) {
            fadeOutTransitionLock.stop();
        }
        fadeOutTransitionLock.play();
    }

    private void alert() {
        exclamationMark.setVisible(true);
        Duration durationMillis = new Duration(200);
        if (fadeInTransitionExclMark == null) {
            fadeInTransitionExclMark = new FadeTransition(Duration.seconds(1), exclamationMark);
            fadeInTransitionExclMark.setFromValue(0.0);
            fadeInTransitionExclMark.setToValue(1.0);
            fadeInTransitionExclMark.setInterpolator(Interpolator.EASE_BOTH);
            fadeInTransitionExclMark.setDuration(durationMillis);
            fadeInTransitionExclMark.setOnFinished(event -> exclamationMark.setVisible(true));
        }
        if (fadeInTransitionExclMark.getStatus().equals(Animation.Status.RUNNING)) {
            fadeInTransitionExclMark.stop();
        }
        fadeInTransitionExclMark.play();
    }

    private void calmAlert() {
        //TODO: needed? -> exclamationMark.setVisible(true);
        Duration durationMillis = new Duration(100);
        if (fadeOutTransitionExclMark == null) {
            fadeOutTransitionExclMark = new FadeTransition(Duration.seconds(1), exclamationMark);
            fadeOutTransitionExclMark.setFromValue(1.0);
            fadeOutTransitionExclMark.setToValue(0.0);
            fadeOutTransitionExclMark.setInterpolator(Interpolator.EASE_BOTH);
            fadeOutTransitionExclMark.setDuration(durationMillis);
            fadeOutTransitionExclMark.setOnFinished(event -> exclamationMark.setVisible(false));
        }
        if (fadeOutTransitionExclMark.getStatus().equals(Animation.Status.RUNNING)) {
            fadeOutTransitionExclMark.stop();
        }
        fadeOutTransitionExclMark.play();
    }

    // ---------------------------------------- AT CONSTRUCTOR SETUP ------------------------------------------------
    // *** INITIALIZER SEQUENCE ***
    @Override
    public void initializeControls() {
        trainSymbol = new Region();
            trainSymbol.setVisible(false);
        inputField = new TextField();
            inputField.setMinWidth(0);
        lockSymbol = new Region();
            lockSymbol.setVisible(false);
        exclamationMark = new Region();
            exclamationMark.setVisible(false);
        underLine = new Rectangle();
        underLineGhost = new Rectangle();
    }

    @Override
    public void initializeLayout() {
        this.setMinSize(MIN_WIDTH, MIN_HEIGHT);
        this.hbox = new HBox(SPACING_FACTOR);
        this.inputPane = new Pane();
    }

    @Override
    public void layoutPanes() {
        this.hbox.getChildren().add(inputPane);
        this.getChildren().add(hbox);
    }

    @Override
    public void layoutControls() {
        this.inputPane.getChildren().addAll(underLineGhost, underLine, exclamationMark, inputField);
        this.hbox.getChildren().add(0, lockSymbol);
        this.hbox.getChildren().add(2, trainSymbol);
        resize(true);
    }

    @Override
    public void addListeners() {
        // *** logic controlling listeners ***
        stateCODEProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue()==State.COMPROMISED.CODE) {
                setState(State.COMPROMISED);
            }
            else if (newValue.intValue()==State.LOCKED.CODE) {
                setState(State.LOCKED);
            }
            else if (newValue.intValue()==State.NEUTRAL.CODE) {
                setState(State.NEUTRAL);
            }
            else if (newValue.intValue()==State.UNVALID.CODE) {
                setState(State.UNVALID);
            }
            else if (newValue.intValue()==State.VALID.CODE) {
                setState(State.VALID);
            }
            else if (newValue.intValue()==State.UNSELECTED.CODE) {
                setState(State.UNSELECTED);
            }
            else {
                setState(State.NEUTRAL);
            }
        });

        // *** visual controlling listeners ***
        heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue()>0.0) resize(true); // (newValue.intValue()>=oldValue.intValue()+TOLERANCE || newValue.intValue()+TOLERANCE<oldValue.intValue())
        });
        widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue()>0.0) resize(true); //if ((newValue.intValue()>=oldValue.intValue()+TOLERANCE || newValue.intValue()+TOLERANCE<oldValue.intValue()) &&
        });
        trainVisibleProperty.addListener((observable, oldValue, newValue) -> {
            double widthFactorByVisibility;
            if (newValue) widthFactorByVisibility=TRAIN_SYMBOL_WIDTH_FACTOR; else widthFactorByVisibility=0.0;
            TRAIN_SYMBOL_WIDTH_FACTOR_PROPERTY.set(widthFactorByVisibility);
            trainSymbol.setVisible(newValue);
            resize(true);
        });
        lockVisibleProperty.addListener((observable, oldValue, newValue) -> {
            double widthFactorByVisibility;
            if (newValue) widthFactorByVisibility=LOCK_SYMBOL_WIDTH_FACTOR; else widthFactorByVisibility=0.0;
            LOCK_SYMBOL_WIDTH_FACTOR_PROPERTY.set(widthFactorByVisibility);
            lockSymbol.setVisible(newValue);
            resize(true);
        });
        inputField.textProperty().addListener(observable -> {
            if (stateCODEProperty.get()==State.UNVALID.CODE) bumpTrain();
        });
    }

    @Override
    public void addBindings() {
    }

    @Override
    public void addEvents() {
        this.setOnMouseClicked(event -> {
            if(!stateCODEProperty.isBound()) {
                int newCODE = (stateCODEProperty.get()+1)%6;
                stateCODEProperty.set(newCODE);
            }
        });
    }

    @Override
    public void applyStylesheet() {
        this.getStyleClass().add("inputField");
        trainSymbol.setId("trainSymbol");
        lockSymbol.setId("lockSymbol");
        exclamationMark.setId("exclamationMark");
        inputField.setId("inputField");
        underLine.setId("underLine");
    }

    @Override
    public void applyJavaFXStyles() {
    }

    // *** RESIZING ***
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
    }

    private void resize(boolean resizeAll) {
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        double spacing = height*SPACING_FACTOR;
        this.hbox.setSpacing(spacing);

        this.inputPane.setLayoutY(0);
        this.inputPane.setLayoutX(0);
        double width = this.getWidth() - getInsets().getLeft() - getInsets().getRight();

        if (width > 0 && height > 0) {
            //double radius = (width>=height) ? height : width;
            //double centerX = width/2;
            //double centerY = height/2;

            double lockWidth = height*LOCK_SYMBOL_WIDTH_FACTOR_PROPERTY.get();
            double exclWidth = height*EXCL_MARK_WIDTH_FACTOR;
            double trainWidth = height*TRAIN_SYMBOL_WIDTH_FACTOR_PROPERTY.get();

            hbox.setMinWidth(lockWidth+exclWidth+trainWidth);

            double inputPaneWidth = width-lockWidth-trainWidth-exclWidth-spacing*3*TRAIN_SYMBOL_WIDTH_FACTOR_PROPERTY.get();

            inputPane.setMinSize(
                    exclWidth,
                    height);
            inputPane.setMaxSize(
                    inputPaneWidth,
                    height);
            inputPane.setPrefSize(
                    inputPaneWidth,
                    height);

            double inputFieldWidth = inputPaneWidth-exclWidth-spacing;

            underLine.setHeight(spacing);
            underLine.setWidth(inputFieldWidth);
            underLineGhost.setHeight(spacing);
            underLineGhost.setWidth(inputFieldWidth);

            inputField.setMaxWidth(inputFieldWidth);
            inputField.setPrefWidth(inputFieldWidth);

            exclamationMark.setLayoutX(inputPaneWidth-exclWidth);
            underLine.setLayoutY(height- spacing+0.4);
            underLineGhost.setLayoutY(height- spacing+0.4);

            if (resizeAll) { // avoid unnecessary recalculation
                inputField.setFont(Font.font(height*FONT_SIZE_FACTOR));
                inputField.setLayoutY(-spacing);

                lockSymbol.setMinSize(lockWidth, height);
                lockSymbol.setMaxSize(lockWidth, height);
                lockSymbol.setPrefSize(lockWidth, height);

                trainSymbol.setMinSize(trainWidth, height);
                trainSymbol.setMaxSize(trainWidth, height);
                trainSymbol.setPrefSize(trainWidth, height);

                exclamationMark.setMinSize(exclWidth, height);
                exclamationMark.setMaxSize(exclWidth, height);
                exclamationMark.setPrefSize(exclWidth, height);
            }
        }
    }

    // ---------------------------------- INPUT VERIFIER INTERFACE -----------------------------------

    public InputVerifierInterface getInputVerifier() {
        return inputVerifier;
    }

    @Override
    public void bind(ObjectProperty property) {
        inputVerifier.bind(property);
    }

    @Override
    public void unbind(ObjectProperty property) {
        inputVerifier.unbind(property);
    }

    @Override
    public void bindBidirectional(ObjectProperty property) {
        inputVerifier.bindBidirectional(property);
    }

    @Override
    public void setSourceProperty(StringProperty source) {
        inputVerifier.setSourceProperty(source);
    }

    @Override
    public void unsetSourceProperty(StringProperty source) {
        inputVerifier.unsetSourceProperty(source);
    }

    @Override
    public ObjectProperty getVerifiedProperty() {
        return inputVerifier.getVerifiedProperty();
    }

    @Override
    public IntegerProperty getStateCodeProperty() {
        return inputVerifier.getStateCodeProperty();
    }

    @Override
    public StringProperty getSourceProperty() {
        return inputVerifier.getSourceProperty();
    }

    @Override
    public void setUnvalidChars(Character[] unvalidChars) {
        inputVerifier.setExpectedChars(unvalidChars);
    }

    @Override
    public void setUnvalidStrings(String[] unvalidStrings) {
        inputVerifier.setUnvalidStrings(unvalidStrings);
    }

    @Override
    public void setExpectedChars(Character[] chars) {
        inputVerifier.setExpectedChars(chars);
    }


    @Override
    public void setExpectedStrings(String[] strings) {
        inputVerifier.setExpectedStrings(strings);
    }

    @Override
    public void setExpectedSequence(Character[] sequence) {
        inputVerifier.setExpectedSequence(sequence);
    }

    @Override
    public String type() {
        return inputVerifier.type();
    }

    @Override
    public State verifyInput(String value) {
        return inputVerifier.verifyInput(value);
    }
}
