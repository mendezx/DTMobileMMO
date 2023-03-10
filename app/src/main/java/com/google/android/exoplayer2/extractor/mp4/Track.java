package com.google.android.exoplayer2.extractor.mp4;

import android.support.annotation.Nullable;
import com.google.android.exoplayer2.Format;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* loaded from: classes.dex */
public final class Track {
    public static final int TRANSFORMATION_CEA608_CDAT = 1;
    public static final int TRANSFORMATION_NONE = 0;
    public final long durationUs;
    @Nullable
    public final long[] editListDurations;
    @Nullable
    public final long[] editListMediaTimes;
    public final Format format;
    public final int id;
    public final long movieTimescale;
    public final int nalUnitLengthFieldLength;
    @Nullable
    private final TrackEncryptionBox[] sampleDescriptionEncryptionBoxes;
    public final int sampleTransformation;
    public final long timescale;
    public final int type;

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface Transformation {
    }

    public Track(int id, int type, long timescale, long movieTimescale, long durationUs, Format format, int sampleTransformation, @Nullable TrackEncryptionBox[] sampleDescriptionEncryptionBoxes, int nalUnitLengthFieldLength, @Nullable long[] editListDurations, @Nullable long[] editListMediaTimes) {
        this.id = id;
        this.type = type;
        this.timescale = timescale;
        this.movieTimescale = movieTimescale;
        this.durationUs = durationUs;
        this.format = format;
        this.sampleTransformation = sampleTransformation;
        this.sampleDescriptionEncryptionBoxes = sampleDescriptionEncryptionBoxes;
        this.nalUnitLengthFieldLength = nalUnitLengthFieldLength;
        this.editListDurations = editListDurations;
        this.editListMediaTimes = editListMediaTimes;
    }

    public TrackEncryptionBox getSampleDescriptionEncryptionBox(int sampleDescriptionIndex) {
        if (this.sampleDescriptionEncryptionBoxes == null) {
            return null;
        }
        return this.sampleDescriptionEncryptionBoxes[sampleDescriptionIndex];
    }
}
