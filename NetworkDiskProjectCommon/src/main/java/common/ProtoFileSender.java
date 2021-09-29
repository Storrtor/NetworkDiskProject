package common;

import common.DataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoFileSender {
    public static void sendFile(Path path, Channel channel, boolean isClientCommand, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        // signal_byte + filename_length(int) + filename + file_length(long)
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 1 + 4 + filenameBytes.length + 8);

        buf.writeByte(DataType.FILE.getFirstMessageByte());

        if(isClientCommand) {
            buf.writeByte(DataType.CLIENT_COMMAND.getFirstMessageByte());
        } else {
            buf.writeByte(DataType.SERVER_COMMAND.getFirstMessageByte());
        }

        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if(finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }


    }
}
