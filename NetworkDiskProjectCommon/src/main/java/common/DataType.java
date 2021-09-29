package common;

public enum DataType {
    EMPTY((byte)-1), CLIENT_COMMAND((byte) 1), SERVER_COMMAND((byte)2), FILE((byte)15), COMMAND_SEND((byte)16), COMMAND_DWNLD((byte) 17) ,COMMAND_DELETE((byte)18);

    byte firstMessageByte;

    DataType(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public byte getFirstMessageByte() {
        return firstMessageByte;
    }

    public static DataType getDataTypeFromByte(byte b) {
        if (b == FILE.firstMessageByte) {
            return FILE;
        }
        if (b == COMMAND_SEND.firstMessageByte) {
            return COMMAND_SEND;
        }
        if (b == COMMAND_DWNLD.firstMessageByte) {
            return COMMAND_DWNLD;
        }
        if (b == COMMAND_DELETE.firstMessageByte) {
            return COMMAND_DELETE;
        }
        return EMPTY;
    }
}