package fx.client;

import common.DataType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ClientAuthHandler extends ChannelInboundHandlerAdapter {

    private static boolean authOk = false;
    private int nextLength;
    private static String nick;

    public static String getNick() {
        return nick;
    }

    public static boolean isAuthOk() {
        return authOk;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (authOk) {
            ctx.fireChannelRead(msg);
        } else {
            ByteBuf buf = ((ByteBuf) msg);
            // Если пользователь авторизирован, отправляем посылку дальше
            byte authByte = buf.readByte();
            if (authByte == DataType.AUTH_OK.getFirstMessageByte()) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get nick length");
                    nextLength = buf.readInt();
                }
                if (buf.readableBytes() >= nextLength) {
                    System.out.println("STATE: Get nick");
                    byte[] nickByte = new byte[nextLength];
                    buf.readBytes(nickByte);
                    String nick = new String(nickByte);
                    System.out.println(nick);
                    authOk = true;
                }
            }
            System.out.println(nick); // ОСТАНОВИЛАСЬ ТУТ

            if (authByte == DataType.AUTH_NOT_OK.getFirstMessageByte()) {

            }
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
