/*
 *
 * Copyright (c) 2015 by FHNW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package main.java.ch.fhnw.ws4c.retroMetro;

/**
 * @author Dieter Holz
 *
 */
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.ch.fhnw.ws4c.retroMetro.indicators.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import main.java.ch.fhnw.ws4c.retroMetro.inputFields.State;
import main.java.ch.fhnw.ws4c.retroMetro.inputFields.TrainInputField;
import main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers.InputVerifierInterface;
import main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers.NumberInputVerifier;
import main.java.ch.fhnw.ws4c.retroMetro.inputVerifiers.TextInputVerifier;


public class Demo extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox rootPane = new VBox();
        rootPane.setSpacing(6);
        rootPane.maxHeightProperty().bind(primaryStage.heightProperty());
        rootPane.minHeightProperty().bind(primaryStage.heightProperty());
        rootPane.maxWidthProperty().bind(primaryStage.maxWidthProperty());

        Led led = new Led(true);
        led.setIsClickable(true);
        led.setMaxHeight(100);
        led.setMinHeight(100);
        //rootPane.getChildren().add(led);
        led = new Led(false);
        led.setIsClickable(true);
        led.setMaxHeight(100);
        led.setMinHeight(100);
        led.setClickableEnablesBlinking(true);
        led.setBlinkRate(1_000_000_000);
        led.setBlinkEnabled(false);
        rootPane.getChildren().add(led);
        rootPane.setAlignment(Pos.CENTER);
        TrainInputField input = new TrainInputField(true);
        input.setMinSize(500, 70);
        rootPane.getChildren().add(input);
        InputVerifierInterface inputVerifier = new NumberInputVerifier(NumberInputVerifier.NumberTypeEnum.Double);
        ((NumberInputVerifier)inputVerifier).setLimit(4.1234567, -3.12345);
        input = new TrainInputField(inputVerifier);
        input.setMinSize(500, 70);
        rootPane.getChildren().add(input);
        inputVerifier = new TextInputVerifier();
        inputVerifier.setExpectedStrings(new String[] {"expected", "@"});
        inputVerifier.setExpectedChars(new Character[] {'@'});
        inputVerifier.setUnvalidStrings(new String[] {";"});
        inputVerifier.setExpectedSequence(new Character[] {'-', '.', '-', ':'});
        input.setMinSize(500, 70);

        input = new TrainInputField(inputVerifier);
        input.setMinSize(500, 50);
        rootPane.getChildren().add(input);
        TextField text = new TextField();
        text.setMinSize(500, 70);
        text.textProperty().bindBidirectional(input.getVerifiedProperty());
        rootPane.getChildren().add(text);

        Scene scene = new Scene(rootPane);

	    primaryStage.setTitle("JavaFX App");
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(300);
        primaryStage.show();
	    primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
