package com.google.android.gms.internal.plus;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelReader;
import com.google.android.gms.internal.plus.zzr;
import java.util.HashSet;

/* loaded from: classes2.dex */
public final class zzt implements Parcelable.Creator<zzr.zza> {
    @Override // android.os.Parcelable.Creator
    public final /* synthetic */ zzr.zza createFromParcel(Parcel parcel) {
        int validateObjectHeader = SafeParcelReader.validateObjectHeader(parcel);
        HashSet hashSet = new HashSet();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < validateObjectHeader) {
            int readHeader = SafeParcelReader.readHeader(parcel);
            int fieldId = SafeParcelReader.getFieldId(readHeader);
            int i4 = 1;
            if (fieldId != 1) {
                i4 = 2;
                if (fieldId != 2) {
                    i4 = 3;
                    if (fieldId != 3) {
                        SafeParcelReader.skipUnknownField(parcel, readHeader);
                    } else {
                        i3 = SafeParcelReader.readInt(parcel, readHeader);
                    }
                } else {
                    i2 = SafeParcelReader.readInt(parcel, readHeader);
                }
            } else {
                i = SafeParcelReader.readInt(parcel, readHeader);
            }
            hashSet.add(Integer.valueOf(i4));
        }
        if (parcel.dataPosition() == validateObjectHeader) {
            return new zzr.zza(hashSet, i, i2, i3);
        }
        StringBuilder sb = new StringBuilder(37);
        sb.append("Overread allowed size end=");
        sb.append(validateObjectHeader);
        throw new SafeParcelReader.ParseException(sb.toString(), parcel);
    }

    @Override // android.os.Parcelable.Creator
    public final /* synthetic */ zzr.zza[] newArray(int i) {
        return new zzr.zza[i];
    }
}
