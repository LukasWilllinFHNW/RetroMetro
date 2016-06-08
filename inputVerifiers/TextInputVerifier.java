package main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers;

import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import main.java.ch.fhnw.ws4c.retroMetro.inputFields.State;
import main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers.InputVerifierInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Lukas W on 02.03.2016.
 */
public class TextInputVerifier implements InputVerifierInterface{

    private final IntegerProperty stateCodeProperty = new SimpleIntegerProperty();

    private final ObjectProperty<String> stringProperty = new SimpleObjectProperty<>();
    private final StringProperty sourceProperty = new SimpleStringProperty();

    private ArrayList<Character> unvalidChars = new ArrayList<>();
    private ArrayList<String> unvalidStrings = new ArrayList<>();
    private ArrayList<Character> expectedChars = new ArrayList<>();
    private ArrayList<String> expectedStrings = new ArrayList<>();
    private ArrayList<Character> expectedSequence = new ArrayList<>();

    private ChangeListener<String> externalListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue observable, String oldValue, String newValue) {
            verifyInput(newValue);
            sourceProperty.setValue(newValue);
        }
    };

    private ChangeListener<String> sourceListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue observable, String oldValue, String newValue) {
            stateCodeProperty.set(verifyInput(newValue).CODE);
            if(stateCodeProperty.get()!=State.UNVALID.CODE
                    && stateCodeProperty.get()!=State.COMPROMISED.CODE) {
                stringProperty.set(newValue);
            }
        }
    };

    // ---------------------------- CONSTRUCOTRS ---------------------------------------
    public TextInputVerifier() {
        stateCodeProperty.set(State.NEUTRAL.CODE);
        addInternalListeners();
    }

    // ------------------------------- INTERFACE ---------------------------------------
    @Override
    public void bind(ObjectProperty property) {
        stringProperty.bind(property);

    }

    @Override
    public void unbind(@Nullable ObjectProperty property) {
        stringProperty.unbind();
        if (stringProperty.isBound())
            stringProperty.unbindBidirectional(property);
    }

    @Override
    public void bindBidirectional(ObjectProperty property) {
        stringProperty.bindBidirectional((ObjectProperty<String>)property);
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
    public ObjectProperty getVerifiedProperty() {

        return stringProperty;
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
        this.unvalidChars.addAll(Arrays.asList(unvalidChars));
    }

    @Override
    public void setUnvalidStrings(String[] unvalidStrings) {
        this.unvalidStrings.addAll(Arrays.asList(unvalidStrings));
    }

    @Override
    public void setExpectedChars(Character[] expectedChars) {
        this.expectedChars.addAll(Arrays.asList(expectedChars));
    }

    @Override
    public void setExpectedStrings(String[] expectedStrings) {
        this.expectedStrings.addAll(Arrays.asList(expectedStrings));
    }

    @Override
    public void setExpectedSequence(Character[] expectedSequence) {
        this.expectedSequence.addAll(Arrays.asList(expectedSequence));
    }

    @Override
    public String type() {
        return this.getClass().getTypeName();
    }

    @Override
    public State verifyInput(String value) {
        State state = State.VALID;
        // value should't contain any of the unvalid characters
        for (Character ch : unvalidChars){
            if (value.toLowerCase().indexOf(ch)> -1)
                return State.UNVALID;
        }
        // Value shouldn't contain any of the unvalid strings
        for (String str : unvalidStrings) {
            if (value.toLowerCase().contains(str))
                return State.UNVALID;
        }

        // Does the value comply to the expected sequence
        state = checkSequence(value);

        if (state!=State.UNVALID&&state!=State.COMPROMISED) {
            // Does value contain all expected chars?
            for (Character ch : expectedChars){
                if (value.toLowerCase().indexOf(ch)<0)
                    return State.COMPROMISED;
            }
            // Does value contain all expected strings?
            for (String str : expectedStrings) {
                if (!value.contains(str))
                    return State.COMPROMISED;
            }
        }

        return state;
    }

    private State checkSequence(String value) {
        State state = State.VALID;
        List<Character> distinctExpSeqChars = expectedSequence.stream().distinct().collect(Collectors.toList());
        List<Character> actualCharSeq = new ArrayList<>(value.length()+2);
        // Only check relevant chars
        for (int i = 0; i < value.length(); i++) {
            if(distinctExpSeqChars.contains(value.charAt(i)))
                actualCharSeq.add(value.charAt(i));
        }
        int e = 0;
        if (actualCharSeq.size()>0) {
            for (int i = 0; i < actualCharSeq.size(); i++, e++) {
                char currentActualChar = actualCharSeq.get(i);
                char currentExpectedChar = expectedSequence.get(e);
                if (currentActualChar!=currentExpectedChar)
                    // Wrong Sequence
                    return State.UNVALID;
                else {
                    if (e == expectedSequence.size()-1)
                        e = -1;
                    else
                        new String("hier");
                    // Check the rest or exit
                }
            }
        }

        if (e>0 && e < expectedSequence.size()-1)
            return State.COMPROMISED;
        return State.VALID;
    }

    private void addInternalListeners() {
        stringProperty.addListener(externalListener);
        sourceProperty.addListener(sourceListener);
    }
}
