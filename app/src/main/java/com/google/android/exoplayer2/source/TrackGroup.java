package com.google.android.exoplayer2.source;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.Assertions;
import java.util.Arrays;

/* loaded from: classes.dex */
public final class TrackGroup implements Parcelable {
    public static final Parcelable.Creator<TrackGroup> CREATOR = new Parcelable.Creator<TrackGroup>() { // from class: com.google.android.exoplayer2.source.TrackGroup.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TrackGroup createFromParcel(Parcel in) {
            return new TrackGroup(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TrackGroup[] newArray(int size) {
            return new TrackGroup[size];
        }
    };
    private final Format[] formats;
    private int hashCode;
    public final int length;

    public TrackGroup(Format... formats) {
        Assertions.checkState(formats.length > 0);
        this.formats = formats;
        this.length = formats.length;
    }

    TrackGroup(Parcel in) {
        this.length = in.readInt();
        this.formats = new Format[this.length];
        for (int i = 0; i < this.length; i++) {
            this.formats[i] = (Format) in.readParcelable(Format.class.getClassLoader());
        }
    }

    public Format getFormat(int index) {
        return this.formats[index];
    }

    public int indexOf(Format format) {
        for (int i = 0; i < this.formats.length; i++) {
            if (format == this.formats[i]) {
                return i;
            }
        }
        return -1;
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            int result = Arrays.hashCode(this.formats) + 527;
            this.hashCode = result;
        }
        return this.hashCode;
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TrackGroup other = (TrackGroup) obj;
        return this.length == other.length && Arrays.equals(this.formats, other.formats);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.length);
        for (int i = 0; i < this.length; i++) {
            dest.writeParcelable(this.formats[i], 0);
        }
    }
}
