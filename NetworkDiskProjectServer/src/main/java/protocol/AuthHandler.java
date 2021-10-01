package protocol;

import common.DataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private int nextLength;
    private static boolean authOkBol = false;
    private static String nick;

    public static String getNick() {
        return nick;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        // Если пользователь авторизирован, отправляем посылку дальше
        System.out.println(authOkBol);

        if (authOkBol) {
            ctx.fireChannelRead(msg);
        } else {
            // Иначе получаем длину сообщения и вычитываем его в стринг
            byte authOk = buf.readByte();
            if (buf.readableBytes() >= 4) {
                System.out.println("STATE: Get auth length");
                nextLength = buf.readInt();
            }

            if (buf.readableBytes() >= nextLength) {
                byte[] strAuth = new byte[nextLength];
                buf.readBytes(strAuth);
                String authStr = new String(strAuth, "UTF-8");
                System.out.println(authStr);
                try {
                    // Идем в бд и авторизируем пользователя + создаем ему папки, если их не существовало
                    String[] parts = authStr.split("\\s+");
                    nick = Server.getAuthService().getNickByLoginAndPass(parts[0], parts[1]);
                    System.out.println("Злогинились");
                    if (nick != null) {
                        File fileClient = new File("client_storage/" + nick);
                        File fileServer = new File("server_storage/" + nick);
                        if(!fileClient.exists()) {
                            fileClient.mkdir();
                        }
                        if(!fileServer.exists()) {
                            fileServer.mkdir();
                        }
                        System.out.println("Создали папки");
                        // Отправляем на клиента сигнал, что пользователь успешно прошел авторизацию
                        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + nick.getBytes(StandardCharsets.UTF_8).length);
                        byteBuf.writeByte(DataType.AUTH_OK.getFirstMessageByte());
                        byteBuf.writeInt(nick.length());
                        byteBuf.writeBytes(nick.getBytes(StandardCharsets.UTF_8));
                        System.out.println("Отправка из ок");
                        ctx.writeAndFlush(byteBuf); //отправка обратно
                        authOkBol = true;
                        return;
                    }
                } catch (ArrayIndexOutOfBoundsException | SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                ByteBuf byteBuf = ctx.alloc().buffer(1);
                byteBuf.writeByte(DataType.AUTH_NOT_OK.getFirstMessageByte());
                ctx.writeAndFlush(byteBuf); //отправка обратно
                System.out.println("Отправка");
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }



    }


}
