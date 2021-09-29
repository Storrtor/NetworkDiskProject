package common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();

                byte isClientCommand = buf.readByte();

                currentState = State.NAME_LENGTH;
                receivedFileLength = 0L;
                System.out.println("State: Start file receiving");

                if (currentState == State.NAME_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        System.out.println("Stage: Get file name");
                        nextLength = buf.readInt();
                        currentState = State.NAME;
                    }
                }

                System.out.println(buf.readableBytes());
                System.out.println(nextLength);
                // Запрос на отправку файла
                if (DataType.getDataTypeFromByte(readed) == DataType.FILE) {

                    if (currentState == State.NAME) {

                        if (buf.readableBytes() >= nextLength) {
                            byte[] fileName = new byte[nextLength];
                            buf.readBytes(fileName);
                            System.out.println("State: filename received - " + new String(fileName, StandardCharsets.UTF_8));

                            if (isClientCommand == DataType.CLIENT_COMMAND.getFirstMessageByte()){
                                out = new BufferedOutputStream(new FileOutputStream("server_storage/" + new String(fileName)));
                            } else {
                                out = new BufferedOutputStream(new FileOutputStream("client_storage/" + new String(fileName)));
                            }

                            currentState = State.FILE_LENGTH;
                        }
                    }

                    if (currentState == State.FILE_LENGTH) {
                        if (buf.readableBytes() >= 8) {
                            fileLength = buf.readLong();
                            System.out.println("State: file length received - " + fileLength);
                            currentState = State.FILE;
                        }
                    }

                    if (currentState == State.FILE) {
                        while (buf.readableBytes() > 0) {
                            out.write(buf.readByte());
                            receivedFileLength++;
                            if (fileLength == receivedFileLength) {
                                currentState = State.IDLE;
                                System.out.println("File received");
                                out.close();
                                break;
                            }
                        }
                    }
                    // Запрос на загрузку файла
                } else if (DataType.getDataTypeFromByte(readed) == DataType.COMMAND_DWNLD) {
                    if (currentState == State.NAME) {
                        if (buf.readableBytes() >= nextLength) {
                            byte[] fileName = new byte[nextLength];
                            buf.readBytes(fileName);
                            System.out.println(fileName);
                            System.out.println("State: filename received - " + new String(fileName, StandardCharsets.UTF_8));
                            ProtoFileSender.sendFile(Paths.get("server_storage/" + new String(fileName)), ctx.channel(), true, future -> {
                                if (!future.isSuccess()) {
                                    future.cause().printStackTrace();
                                }
                                if (future.isSuccess()) {
                                    System.out.println("Файл успешно передан");
                                }
                            });
                        }
                    }

                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
