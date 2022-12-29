package com.google.android.gms.auth.api.phone;

import android.app.Activity;
import android.content.Context;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.internal.p000authapiphone.zzw;
import com.google.android.gms.tasks.Task;

/* compiled from: com.google.android.gms:play-services-auth-api-phone@@17.5.1 */
/* loaded from: classes3.dex */
public abstract class SmsRetrieverClient extends GoogleApi<Api.ApiOptions.NoOptions> implements SmsRetrieverApi {
    private static final Api.ClientKey<zzw> zza = new Api.ClientKey<>();
    private static final Api.AbstractClientBuilder<zzw, Api.ApiOptions.NoOptions> zzb = new zza();
    private static final Api<Api.ApiOptions.NoOptions> zzc = new Api<>("SmsRetriever.API", zzb, zza);

    public SmsRetrieverClient(Activity activity) {
        super(activity, zzc, Api.ApiOptions.NO_OPTIONS, GoogleApi.Settings.DEFAULT_SETTINGS);
    }

    @Override // com.google.android.gms.auth.api.phone.SmsRetrieverApi
    public abstract Task<Void> startSmsRetriever();

    @Override // com.google.android.gms.auth.api.phone.SmsRetrieverApi
    public abstract Task<Void> startSmsUserConsent(String str);

    public SmsRetrieverClient(Context context) {
        super(context, zzc, Api.ApiOptions.NO_OPTIONS, GoogleApi.Settings.DEFAULT_SETTINGS);
    }
}
