package com.jawi;

import com.jawi.game.GameController;
import com.jawi.usb.UsbProxy;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    private static final String formatStr = "%03d";

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();

        GameController.get().setView(controller);
        UsbProxy.get().addListener(GameController.get());

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().addAll(this.getClass().getResource("/style.css").toExternalForm());

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                GameController.get().next();
            }
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            while(true) {
                try {
                    Thread.sleep(70000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    //GameController.get().next();
                });
            }
        });
        //primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);

        //primaryStage.setFullScreen(true);
        primaryStage.show();

        Node thumb = controller.getCalBurn().lookup(".thumb");
        controller.getTestLabel().setLayoutX(thumb.getLayoutX()-3);
        controller.getTestLabel().setLayoutY(thumb.getLayoutY() + 60);

        controller.getCalBurn().valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                controller.getTestLabel().setText(String.format(formatStr, (int)controller.getCalBurn().getValue()));
                controller.getTestLabel().setLayoutX(thumb.getLayoutX()-3);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
