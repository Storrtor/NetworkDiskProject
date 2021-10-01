package fx.client;

import common.DataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.nio.charset.StandardCharsets;

public class ModalWindow {

    public static void newWindowAuth(String title) {

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        Pane pane = new TilePane();

        TextField logField = new TextField();
        TextField passField = new TextField();

        Button button = new Button("Войти");
        Label logLabel = new Label("Введите логин");
        Label passLabel = new Label("Введите пароль");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String auth = logField.getText() + " " + passField.getText();
                byte[] authArray = auth.getBytes(StandardCharsets.UTF_8);
                ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(authArray.length);
                buf.writeByte(DataType.AUTH_NOT_OK.getFirstMessageByte());
                buf.writeInt(authArray.length);
                buf.writeBytes(authArray);
                System.out.println("Авторизация с клиента");
                Network.getInstance().getCurrentChannel().writeAndFlush(buf);
                logField.clear();
                passField.clear();
                window.close();
            }
        });


        pane.getChildren().addAll(logLabel, logField, passLabel, passField, button);


        Scene scene = new Scene(pane, 170, 150);
        window.setResizable(false);
        window.setScene(scene);
        window.setTitle(title);
        window.showAndWait();

    }

    public static void newWindowSignIn(String title) {

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        Pane pane = new Pane();

        Button button = new Button("close");
        pane.getChildren().add(button);

        Scene scene = new Scene(pane, 400, 200);
        window.setScene(scene);
        window.setTitle(title);
        window.showAndWait();

    }


}
