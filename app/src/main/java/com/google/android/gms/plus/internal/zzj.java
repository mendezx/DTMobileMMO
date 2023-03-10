package com.google.android.gms.plus.internal;

import android.app.PendingIntent;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.BaseImplementation;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.internal.BaseGmsClient;
import com.google.android.gms.plus.People;

/* loaded from: classes2.dex */
final class zzj extends zza {
    private final BaseImplementation.ResultHolder<People.LoadPeopleResult> zzv;

    public zzj(BaseImplementation.ResultHolder<People.LoadPeopleResult> resultHolder) {
        this.zzv = resultHolder;
    }

    @Override // com.google.android.gms.plus.internal.zza, com.google.android.gms.plus.internal.zzb
    public final void zza(DataHolder dataHolder, String str) {
        Status status = new Status(dataHolder.getStatusCode(), (String) null, dataHolder.getMetadata() != null ? (PendingIntent) dataHolder.getMetadata().getParcelable(BaseGmsClient.KEY_PENDING_INTENT) : null);
        if (!status.isSuccess() && dataHolder != null) {
            if (!dataHolder.isClosed()) {
                dataHolder.close();
            }
            dataHolder = null;
        }
        this.zzv.setResult(new zzi(status, dataHolder, str));
    }
}
