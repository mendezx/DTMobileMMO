package com.google.android.gms.internal.games;

import com.google.android.gms.common.api.internal.RemoteCall;
import com.google.android.gms.tasks.TaskCompletionSource;

/* compiled from: com.google.android.gms:play-services-games@@23.0.0 */
/* loaded from: classes3.dex */
public final /* synthetic */ class zzai implements RemoteCall {
    public static final /* synthetic */ zzai zza = new zzai();

    private /* synthetic */ zzai() {
    }

    @Override // com.google.android.gms.common.api.internal.RemoteCall
    public final void accept(Object obj, Object obj2) {
        int i = zzas.zza;
        ((TaskCompletionSource) obj2).setResult(((com.google.android.gms.games.internal.zzas) ((com.google.android.gms.games.internal.zzan) obj).getService()).zzh());
    }
}
