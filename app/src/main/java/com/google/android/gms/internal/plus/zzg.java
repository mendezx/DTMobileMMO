package com.google.android.gms.internal.plus;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

/* loaded from: classes2.dex */
abstract class zzg extends Plus.zza<Status> {
    private zzg(GoogleApiClient googleApiClient) {
        super(googleApiClient);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public /* synthetic */ zzg(GoogleApiClient googleApiClient, zzf zzfVar) {
        this(googleApiClient);
    }

    @Override // com.google.android.gms.common.api.internal.BasePendingResult
    public /* synthetic */ Result createFailedResult(Status status) {
        return status;
    }
}
