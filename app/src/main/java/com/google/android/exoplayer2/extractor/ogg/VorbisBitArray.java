package com.google.android.exoplayer2.extractor.ogg;

import com.google.android.exoplayer2.util.Assertions;
import kotlin.UByte;

/* loaded from: classes.dex */
final class VorbisBitArray {
    private int bitOffset;
    private final int byteLimit;
    private int byteOffset;
    private final byte[] data;

    public VorbisBitArray(byte[] data) {
        this.data = data;
        this.byteLimit = data.length;
    }

    public void reset() {
        this.byteOffset = 0;
        this.bitOffset = 0;
    }

    public boolean readBit() {
        boolean returnValue = (((this.data[this.byteOffset] & UByte.MAX_VALUE) >> this.bitOffset) & 1) == 1;
        skipBits(1);
        return returnValue;
    }

    public int readBits(int numBits) {
        int tempByteOffset = this.byteOffset;
        int bitsRead = Math.min(numBits, 8 - this.bitOffset);
        int tempByteOffset2 = tempByteOffset + 1;
        int returnValue = ((this.data[tempByteOffset] & UByte.MAX_VALUE) >> this.bitOffset) & (255 >> (8 - bitsRead));
        while (bitsRead < numBits) {
            returnValue |= (this.data[tempByteOffset2] & UByte.MAX_VALUE) << bitsRead;
            bitsRead += 8;
            tempByteOffset2++;
        }
        int returnValue2 = returnValue & ((-1) >>> (32 - numBits));
        skipBits(numBits);
        return returnValue2;
    }

    public void skipBits(int numBits) {
        int numBytes = numBits / 8;
        this.byteOffset += numBytes;
        this.bitOffset += numBits - (numBytes * 8);
        if (this.bitOffset > 7) {
            this.byteOffset++;
            this.bitOffset -= 8;
        }
        assertValidOffset();
    }

    public int getPosition() {
        return (this.byteOffset * 8) + this.bitOffset;
    }

    public void setPosition(int position) {
        this.byteOffset = position / 8;
        this.bitOffset = position - (this.byteOffset * 8);
        assertValidOffset();
    }

    public int bitsLeft() {
        return ((this.byteLimit - this.byteOffset) * 8) - this.bitOffset;
    }

    private void assertValidOffset() {
        Assertions.checkState(this.byteOffset >= 0 && (this.byteOffset < this.byteLimit || (this.byteOffset == this.byteLimit && this.bitOffset == 0)));
    }
}
