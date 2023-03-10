package com.google.android.gms.internal.plus;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.model.people.PersonBuffer;

/* loaded from: classes2.dex */
final class zzq implements People.LoadPeopleResult {
    private final /* synthetic */ Status zzan;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzq(zzp zzpVar, Status status) {
        this.zzan = status;
    }

    @Override // com.google.android.gms.plus.People.LoadPeopleResult
    public final String getNextPageToken() {
        return null;
    }

    @Override // com.google.android.gms.plus.People.LoadPeopleResult
    public final PersonBuffer getPersonBuffer() {
        return null;
    }

    @Override // com.google.android.gms.common.api.Result
    public final Status getStatus() {
        return this.zzan;
    }

    @Override // com.google.android.gms.common.api.Releasable
    public final void release() {
    }
}
