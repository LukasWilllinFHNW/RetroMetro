package main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers;

import com.sun.istack.internal.Nullable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import main.java.ch.fhnw.ws4c.retroMetro.inputFields.State;
import main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers.InputVerifierInterface;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Type;
import java.util.Calendar;

/**
 * Created by Lukas W on 02.03.2016.
 */
public class NumberInputVerifier implements InputVerifierInterface {

    private final IntegerProperty stateCodeProperty = new SimpleIntegerProperty(State.NEUTRAL.CODE);
    private final NumberTypeEnum numberType;

    private final IntegerProperty integerProperty = new SimpleIntegerProperty();
    private final DoubleProperty doubleProperty = new SimpleDoubleProperty();
    private final LongProperty longProperty = new SimpleLongProperty();
    private final FloatProperty floatProperty = new SimpleFloatProperty();
    private final ObjectProperty<Number> numberObjectProperty = new SimpleObjectProperty<Number>();

    private final StringProperty sourceProperty = new SimpleStringProperty();

    public final DoubleProperty upperLimitProperty = new SimpleDoubleProperty(0);
    public final DoubleProperty lowerLimitProperty = new SimpleDoubleProperty(0);

    private ChangeListener<Number> externalListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue observable, Number oldValue, Number newValue) {
            numberObjectProperty.set(newValue);
            switch (numberType) {
                case Integer:
                    integerProperty.set(numberObjectProperty.get().intValue());
                    break;
                case Short:
                    integerProperty.set(numberObjectProperty.get().shortValue());
                    break;
                case Byte:
                    integerProperty.set(numberObjectProperty.get().byteValue());
                    break;
                default: break;
            }
            longProperty.set(numberObjectProperty.get().longValue());
            doubleProperty.set(numberObjectProperty.get().doubleValue());
            floatProperty.set(numberObjectProperty.get().floatValue());
        }
    };

    private ChangeListener<String> sourceListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue observable, String oldValue, String newValue) {
            Number number = tryParse(newValue);
            if (number != null) {
                numberObjectProperty.set(number);
            }
        }
    };

    // ------------------------------------ CONSTRUCTORS --------------------------------

    public NumberInputVerifier(NumberTypeEnum numberType) {
        this.numberType = numberType;
        sourceProperty.addListener(sourceListener);
        stateCodeProperty.set(State.UNSELECTED.CODE);
        stateCodeProperty.set(State.NEUTRAL.CODE);
        addInternalListeners();
    }

    public enum NumberTypeEnum {
        Double,
        Float,
        Integer,
        Long,
        Short,
        Byte
    }

    // ------------------------------ PUBLIC API ---------------------------------------
    public void setUpperLimit(double upperLimit) {
        upperLimitProperty.set(upperLimit);
    }
    public void setLowerLimit(double lowerLimit) {
        lowerLimitProperty.set(lowerLimit);
    }
    public void setLimit(double upperLimit, double lowerLimit) {
        upperLimitProperty.set(upperLimit);
        lowerLimitProperty.set(lowerLimit);
    }

    // ----------------------------------- INTERFACE ------------------------------------
    @Override
    public void bind(ObjectProperty property) {
        numberObjectProperty.bind((ObjectProperty<Number>)property);
    }
    @Override
    public void unbind(@Nullable ObjectProperty property) {
        numberObjectProperty.unbind();
        if (numberObjectProperty.isBound())
            numberObjectProperty.unbindBidirectional(property);
    }
    @Override
    public void bindBidirectional(ObjectProperty property) {
        numberObjectProperty.bindBidirectional((ObjectProperty<Number>)property);
    }

    @Override
    public ObjectProperty<Number> getVerifiedProperty() {
        return numberObjectProperty;
    }

    public IntegerProperty getIntegerProperty() {
        return integerProperty;
    }
    public DoubleProperty getDoubleProperty() {
        return doubleProperty;
    }
    public FloatProperty getFloatProperty() {
        return floatProperty;
    }
    public LongProperty getLongProperty() {
        return longProperty;
    }

    @Override
    public void setSourceProperty(StringProperty source) {
        sourceProperty.bindBidirectional(source);
    }
    @Override
    public void unsetSourceProperty(StringProperty source) {
        sourceProperty.unbindBidirectional(source);
    }


    @Override
    public IntegerProperty getStateCodeProperty() {
        return stateCodeProperty;
    }

    @Override
    public StringProperty getSourceProperty() {
        return sourceProperty;
    }

    @Override
    public void setUnvalidChars(Character[] unvalidChars) {

    }

    //@Override
    //public void setUnvalidChars(char[] unvalidChars) {
    //    throw new NotImplementedException();
    //}

    @Override
    public void setUnvalidStrings(String[] unvalidChars) {
        throw new NotImplementedException();
    }

    @Override
    public void setExpectedChars(Character[] chars) {

    }

    @Override
    public void setExpectedStrings(String[] strings) {
        throw new NotImplementedException();
    }

    @Override
    public void setExpectedSequence(Character[] sequence) {
        throw new NotImplementedException();
    }

    @Override
    public String type() {
        return this.getClass().getTypeName();
    }

    @Override
    public State verifyInput(String value) {
        tryParse(value);
        return State.getStateByCode(stateCodeProperty.get());
    }

    private Number tryParse(String value) {
        Number number = null;
        try {
            switch (numberType) {
                case Byte:
                    number = Byte.parseByte(value);
                    break;
                case Short:
                    number = Short.parseShort(value);
                    break;
                case Integer:
                    number = Integer.parseInt(value);
                    break;
                case Long:
                    number = Long.parseLong(value);
                    break;
                case Float:
                    number = Float.parseFloat(value);
                    break;
                case Double:
                    number = Double.parseDouble(value);
                    break;
                default:
                    number = Double.parseDouble(value);
                    break;
            }
        } catch (Exception e) {
            stateCodeProperty.set(State.UNVALID.CODE);
            return number;
        }
        stateCodeProperty.set(State.NEUTRAL.CODE);
        if (number.doubleValue()>upperLimitProperty.get())
            stateCodeProperty.set(State.UNVALID.CODE);
        if (number.doubleValue()<lowerLimitProperty.get())
            stateCodeProperty.set(State.UNVALID.CODE);
        return number;
    }

    private void addInternalListeners() {
        switch (numberType) {
            case Byte:
                integerProperty.addListener(externalListener);
                numberObjectProperty.addListener(externalListener);
                break;
            case Short:
                integerProperty.addListener(externalListener);
                numberObjectProperty.addListener(externalListener);
                break;
            case Integer:
                integerProperty.addListener(externalListener);
                numberObjectProperty.addListener(externalListener);
                break;
            case Long:
                longProperty.addListener(externalListener);
                numberObjectProperty.addListener(externalListener);
                break;
            case Float:
                floatProperty.addListener(externalListener);
                numberObjectProperty.addListener(externalListener);
                break;
            case Double:
                doubleProperty.addListener(externalListener);
                numberObjectProperty.addListener(externalListener);
                break;
            default:
                numberObjectProperty.addListener(externalListener);
                break;
        }
    }
}
