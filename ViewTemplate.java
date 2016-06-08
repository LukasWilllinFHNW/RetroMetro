package main.java.ch.fhnw.ws4c.retroMetro;

/**
 * Created by Lukas on 15.12.2015.
 */
public interface ViewTemplate {

    default void initSequence() {
        initializeControls();
        initializeLayout();
        layoutPanes();
        layoutControls();
        addListeners();
        addBindings();
        addEvents();
        applyStylesheet();
        applyJavaFXStyles();
    }

    void initializeControls() ;

    void initializeLayout() ;

    void layoutPanes() ;

    void layoutControls() ;

    void addListeners() ;

    void addBindings() ;

    void addEvents() ;

    void applyStylesheet() ;

    void applyJavaFXStyles() ;
}
