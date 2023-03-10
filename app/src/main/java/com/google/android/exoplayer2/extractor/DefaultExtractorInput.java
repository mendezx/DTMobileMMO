package com.google.android.exoplayer2.extractor;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/* loaded from: classes.dex */
public final class DefaultExtractorInput implements ExtractorInput {
    private static final int PEEK_MAX_FREE_SPACE = 524288;
    private static final int PEEK_MIN_FREE_SPACE_AFTER_RESIZE = 65536;
    private static final int SCRATCH_SPACE_SIZE = 4096;
    private final DataSource dataSource;
    private int peekBufferLength;
    private int peekBufferPosition;
    private long position;
    private final long streamLength;
    private byte[] peekBuffer = new byte[65536];
    private final byte[] scratchSpace = new byte[4096];

    public DefaultExtractorInput(DataSource dataSource, long position, long length) {
        this.dataSource = dataSource;
        this.position = position;
        this.streamLength = length;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public int read(byte[] target, int offset, int length) throws IOException, InterruptedException {
        int bytesRead = readFromPeekBuffer(target, offset, length);
        if (bytesRead == 0) {
            bytesRead = readFromDataSource(target, offset, length, 0, true);
        }
        commitBytesRead(bytesRead);
        return bytesRead;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public boolean readFully(byte[] target, int offset, int length, boolean allowEndOfInput) throws IOException, InterruptedException {
        int bytesRead = readFromPeekBuffer(target, offset, length);
        while (bytesRead < length && bytesRead != -1) {
            bytesRead = readFromDataSource(target, offset, length, bytesRead, allowEndOfInput);
        }
        commitBytesRead(bytesRead);
        return bytesRead != -1;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public void readFully(byte[] target, int offset, int length) throws IOException, InterruptedException {
        readFully(target, offset, length, false);
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public int skip(int length) throws IOException, InterruptedException {
        int bytesSkipped = skipFromPeekBuffer(length);
        if (bytesSkipped == 0) {
            bytesSkipped = readFromDataSource(this.scratchSpace, 0, Math.min(length, this.scratchSpace.length), 0, true);
        }
        commitBytesRead(bytesSkipped);
        return bytesSkipped;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public boolean skipFully(int length, boolean allowEndOfInput) throws IOException, InterruptedException {
        int bytesSkipped = skipFromPeekBuffer(length);
        while (bytesSkipped < length && bytesSkipped != -1) {
            int minLength = Math.min(length, this.scratchSpace.length + bytesSkipped);
            bytesSkipped = readFromDataSource(this.scratchSpace, -bytesSkipped, minLength, bytesSkipped, allowEndOfInput);
        }
        commitBytesRead(bytesSkipped);
        return bytesSkipped != -1;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public void skipFully(int length) throws IOException, InterruptedException {
        skipFully(length, false);
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public boolean peekFully(byte[] target, int offset, int length, boolean allowEndOfInput) throws IOException, InterruptedException {
        if (!advancePeekPosition(length, allowEndOfInput)) {
            return false;
        }
        System.arraycopy(this.peekBuffer, this.peekBufferPosition - length, target, offset, length);
        return true;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public void peekFully(byte[] target, int offset, int length) throws IOException, InterruptedException {
        peekFully(target, offset, length, false);
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public boolean advancePeekPosition(int length, boolean allowEndOfInput) throws IOException, InterruptedException {
        ensureSpaceForPeek(length);
        int bytesPeeked = Math.min(this.peekBufferLength - this.peekBufferPosition, length);
        while (bytesPeeked < length) {
            bytesPeeked = readFromDataSource(this.peekBuffer, this.peekBufferPosition, length, bytesPeeked, allowEndOfInput);
            if (bytesPeeked == -1) {
                return false;
            }
        }
        this.peekBufferPosition += length;
        this.peekBufferLength = Math.max(this.peekBufferLength, this.peekBufferPosition);
        return true;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public void advancePeekPosition(int length) throws IOException, InterruptedException {
        advancePeekPosition(length, false);
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public void resetPeekPosition() {
        this.peekBufferPosition = 0;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public long getPeekPosition() {
        return this.position + this.peekBufferPosition;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public long getPosition() {
        return this.position;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public long getLength() {
        return this.streamLength;
    }

    @Override // com.google.android.exoplayer2.extractor.ExtractorInput
    public <E extends Throwable> void setRetryPosition(long position, E e) throws Throwable {
        Assertions.checkArgument(position >= 0);
        this.position = position;
        throw e;
    }

    private void ensureSpaceForPeek(int length) {
        int requiredLength = this.peekBufferPosition + length;
        if (requiredLength > this.peekBuffer.length) {
            int newPeekCapacity = Util.constrainValue(this.peekBuffer.length * 2, 65536 + requiredLength, 524288 + requiredLength);
            this.peekBuffer = Arrays.copyOf(this.peekBuffer, newPeekCapacity);
        }
    }

    private int skipFromPeekBuffer(int length) {
        int bytesSkipped = Math.min(this.peekBufferLength, length);
        updatePeekBuffer(bytesSkipped);
        return bytesSkipped;
    }

    private int readFromPeekBuffer(byte[] target, int offset, int length) {
        if (this.peekBufferLength == 0) {
            return 0;
        }
        int peekBytes = Math.min(this.peekBufferLength, length);
        System.arraycopy(this.peekBuffer, 0, target, offset, peekBytes);
        updatePeekBuffer(peekBytes);
        return peekBytes;
    }

    private void updatePeekBuffer(int bytesConsumed) {
        this.peekBufferLength -= bytesConsumed;
        this.peekBufferPosition = 0;
        byte[] newPeekBuffer = this.peekBuffer;
        if (this.peekBufferLength < this.peekBuffer.length - 524288) {
            newPeekBuffer = new byte[this.peekBufferLength + 65536];
        }
        System.arraycopy(this.peekBuffer, bytesConsumed, newPeekBuffer, 0, this.peekBufferLength);
        this.peekBuffer = newPeekBuffer;
    }

    private int readFromDataSource(byte[] target, int offset, int length, int bytesAlreadyRead, boolean allowEndOfInput) throws InterruptedException, IOException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        int bytesRead = this.dataSource.read(target, offset + bytesAlreadyRead, length - bytesAlreadyRead);
        if (bytesRead == -1) {
            if (bytesAlreadyRead == 0 && allowEndOfInput) {
                return -1;
            }
            throw new EOFException();
        }
        return bytesAlreadyRead + bytesRead;
    }

    private void commitBytesRead(int bytesRead) {
        if (bytesRead != -1) {
            this.position += bytesRead;
        }
    }
}
