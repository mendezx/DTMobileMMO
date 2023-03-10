package com.google.android.exoplayer2.source.chunk;

import android.util.Log;
import com.google.android.exoplayer2.extractor.DummyTrackOutput;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.source.SampleQueue;
import com.google.android.exoplayer2.source.chunk.ChunkExtractorWrapper;

/* loaded from: classes.dex */
public final class BaseMediaChunkOutput implements ChunkExtractorWrapper.TrackOutputProvider {
    private static final String TAG = "BaseMediaChunkOutput";
    private final SampleQueue[] sampleQueues;
    private final int[] trackTypes;

    public BaseMediaChunkOutput(int[] trackTypes, SampleQueue[] sampleQueues) {
        this.trackTypes = trackTypes;
        this.sampleQueues = sampleQueues;
    }

    @Override // com.google.android.exoplayer2.source.chunk.ChunkExtractorWrapper.TrackOutputProvider
    public TrackOutput track(int id, int type) {
        for (int i = 0; i < this.trackTypes.length; i++) {
            if (type == this.trackTypes[i]) {
                return this.sampleQueues[i];
            }
        }
        Log.e(TAG, "Unmatched track of type: " + type);
        return new DummyTrackOutput();
    }

    public int[] getWriteIndices() {
        int[] writeIndices = new int[this.sampleQueues.length];
        for (int i = 0; i < this.sampleQueues.length; i++) {
            if (this.sampleQueues[i] != null) {
                writeIndices[i] = this.sampleQueues[i].getWriteIndex();
            }
        }
        return writeIndices;
    }

    public void setSampleOffsetUs(long sampleOffsetUs) {
        SampleQueue[] sampleQueueArr;
        for (SampleQueue sampleQueue : this.sampleQueues) {
            if (sampleQueue != null) {
                sampleQueue.setSampleOffsetUs(sampleOffsetUs);
            }
        }
    }
}
