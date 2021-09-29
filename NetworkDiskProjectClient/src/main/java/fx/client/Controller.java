package fx.client;

import common.DataType;
import common.ProtoFileSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    public boolean isClientCommand() {
        return isClientCommand;
    }

    private Network network;

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
            refreshLocalFilesList();
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



//        try {
//            ProtoFileSender.sendFile(Paths.get("demo.txt"), Network.getInstance().getCurrentChannel(), future -> {
//                if (!future.isSuccess()) {
//                    future.cause().printStackTrace();
//    //                Network.getInstance().stop();
//                }
//                if (future.isSuccess()) {
//                    System.out.println("Файл успешно передан");
//    //                Network.getInstance().stop();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        Thread.sleep(2000);
//        try {
//            ProtoFileSender.sendFile(Paths.get("demo1.txt"), Network.getInstance().getCurrentChannel(), future -> {
//                if (!future.isSuccess()) {
//                    future.cause().printStackTrace();
//    //                Network.getInstance().stop();
//                }
//                if (future.isSuccess()) {
//                    System.out.println("Файл успешно передан");
//    //                Network.getInstance().stop();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void pressOnSendButton(ActionEvent actionEvent) throws IOException {
        sendFile(tfFileName);
        tfFileName.clear();
        refreshLocalFilesList();
    }

    public static void sendFile(TextField tfFileName) throws IOException {
        ProtoFileSender.sendFile(Paths.get("client_storage/" + tfFileName.getText()),  Network.getInstance().getCurrentChannel(), isClientCommand, future -> {
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
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);
        buf.writeByte(DataType.COMMAND_DWNLD.getFirstMessageByte());
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        System.out.println("Загрузка");
        Network.getInstance().getCurrentChannel().writeAndFlush(buf);
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
                Files.list(Paths.get("client_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> clientFilesList.getItems().add(o));
                serverFilesList.getItems().clear();
                Files.list(Paths.get("server_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> serverFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

