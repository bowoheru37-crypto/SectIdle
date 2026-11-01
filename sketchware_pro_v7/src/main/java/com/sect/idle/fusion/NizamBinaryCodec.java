package com.sect.idle.fusion;

public class NizamBinaryCodec {
    public static final int MAGIC_NIMOVE = 0x4E4D4F56;

    public static byte[] encodeMove(NizamMoveData move) {
        if (move == null) return new byte[0];
        byte[] buffer = new byte[32];
        writeInt(buffer, 0, MAGIC_NIMOVE);
        buffer[4] = (byte) move.startupFrames;
        buffer[5] = (byte) move.activeFrames;
        buffer[6] = (byte) move.recoveryFrames;
        buffer[7] = (byte) ((int) move.damage & 0xFF);

        writeInt(buffer, 8, (int) move.activeHitbox.x);
        writeInt(buffer, 12, (int) move.activeHitbox.y);
        writeInt(buffer, 16, (int) move.activeHitbox.width);
        writeInt(buffer, 20, (int) move.activeHitbox.height);
        return buffer;
    }

    public static NizamMoveData decodeMove(byte[] buffer) {
        if (buffer == null || buffer.length < 24) return new NizamMoveData();
        int magic = readInt(buffer, 0);
        if (magic != MAGIC_NIMOVE) return new NizamMoveData();

        NizamMoveData move = new NizamMoveData();
        move.startupFrames = buffer[4] & 0xFF;
        move.activeFrames = buffer[5] & 0xFF;
        move.recoveryFrames = buffer[6] & 0xFF;
        move.damage = buffer[7] & 0xFF;

        move.activeHitbox.x = readInt(buffer, 8);
        move.activeHitbox.y = readInt(buffer, 12);
        move.activeHitbox.width = readInt(buffer, 16);
        move.activeHitbox.height = readInt(buffer, 20);
        return move;
    }

    private static void writeInt(byte[] b, int offset, int val) {
        b[offset] = (byte) ((val >> 24) & 0xFF);
        b[offset + 1] = (byte) ((val >> 16) & 0xFF);
        b[offset + 2] = (byte) ((val >> 8) & 0xFF);
        b[offset + 3] = (byte) (val & 0xFF);
    }

    private static int readInt(byte[] b, int offset) {
        return ((b[offset] & 0xFF) << 24) |
               ((b[offset + 1] & 0xFF) << 16) |
               ((b[offset + 2] & 0xFF) << 8) |
               (b[offset + 3] & 0xFF);
    }
}
