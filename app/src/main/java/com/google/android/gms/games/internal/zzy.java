package com.google.android.gms.games.internal;

import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.GamesStatusUtils;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.tasks.TaskCompletionSource;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-games@@23.0.0 */
/* loaded from: classes3.dex */
public final class zzy extends zza {
    private final TaskCompletionSource zza;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzy(TaskCompletionSource taskCompletionSource) {
        this.zza = taskCompletionSource;
    }

    @Override // com.google.android.gms.games.internal.zza, com.google.android.gms.games.internal.zzap
    public final void zzj(DataHolder dataHolder) {
        int statusCode = dataHolder.getStatusCode();
        boolean z = statusCode == 3;
        if (statusCode == 0 || z) {
            this.zza.setResult(new AnnotatedData(new LeaderboardBuffer(dataHolder), z));
            return;
        }
        GamesStatusUtils.zza(this.zza, statusCode);
        dataHolder.close();
    }
}
