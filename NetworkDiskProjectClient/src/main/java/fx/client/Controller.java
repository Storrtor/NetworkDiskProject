package fx.client;

import common.DataType;
import common.ProtoFileSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    private static boolean isClientCommand = false;

    @FXML
    Button signIn;

    @FXML
    Button signUp;

    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> clientFilesList;
    @FXML
    ListView<String> serverFilesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressOnSendButton(ActionEvent actionEvent) throws IOException {
        sendFile(ClientAuthHandler.getNick() + "/"  + tfFileName.getText());
        tfFileName.clear();
        refreshLocalFilesList();
    }

    // Отправка файла на сервер
    public static void sendFile(String tfFileName) throws IOException {
        System.out.println(tfFileName);
        ProtoFileSender.sendFile(Paths.get("client_storage/" + tfFileName),  Network.getInstance().getCurrentChannel(), isClientCommand, future -> {
            if(!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if(future.isSuccess()) {
                System.out.println("Файл успешно передан");
            }
        });
    }


    public void pressOnDownloud(ActionEvent actionEvent) {
        isClientCommand = true;
        byte[] filenameBytes = tfFileName.getText().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + 1 + filenameBytes.length);
        buf.writeByte(DataType.COMMAND_DWNLD.getFirstMessageByte());
        buf.writeByte(DataType.CLIENT_COMMAND.getFirstMessageByte());
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        System.out.println("Загрузка");
        Network.getInstance().getCurrentChannel().writeAndFlush(buf);
        tfFileName.clear();
    }

    public void pressOnDelete(ActionEvent actionEvent) {
    }

    /**
     * Рефреш списка файлов
     */
    public void refreshLocalFilesList() {
        Platform.runLater(() -> {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client_storage/" + ClientAuthHandler.getNick()))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> clientFilesList.getItems().add(o));
                serverFilesList.getItems().clear();
                Files.list(Paths.get("server_storage/" + ClientAuthHandler.getNick()))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> serverFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void pressOnRefresh(ActionEvent actionEvent) {
        refreshLocalFilesList();
        tfFileName.clear();
    }

    public void pressOnAuth(ActionEvent actionEvent) throws InterruptedException {
        ModalWindow.newWindowAuth("Авторизация");
        if (ClientAuthHandler.isAuthOk()) {
            signUp.setVisible(false);
            signUp.setManaged(false);
        }

    }

    public void pressOnSignUn(ActionEvent actionEvent) {
        ModalWindow.newWindowSignIn("Регистрация");
        signIn.setVisible(false);
        signIn.setManaged(false);
    }
}

