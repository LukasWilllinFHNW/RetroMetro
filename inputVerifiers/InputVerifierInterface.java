package main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers;

import com.sun.istack.internal.Nullable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import main.java.ch.fhnw.ws4c.retroMetro.inputFields.State;

import java.lang.reflect.Type;

/**
 * Created by Lukas W on 07.06.2016.
 */
public interface InputVerifierInterface {


    // TODO vielleicht benötigt jeder verifier eine eigene implementation und nicht objectProperty<?>
    // public api
    public void bind(ObjectProperty property);

    public void unbind(ObjectProperty property);

    public void bindBidirectional(@Nullable ObjectProperty property);

    public void setSourceProperty(StringProperty source);

    public void unsetSourceProperty(StringProperty source);

    public ObjectProperty getVerifiedProperty();

    public IntegerProperty getStateCodeProperty();

    public StringProperty getSourceProperty();

    public void setUnvalidChars(Character[] unvalidChars);

    public void setUnvalidStrings(String[] unvalidStrings);

    public void setExpectedChars(Character[] chars);

    public void setExpectedStrings(String[] strings);

    public void setExpectedSequence(Character[] sequence);

    public String type();

    public State verifyInput(String value);
}
