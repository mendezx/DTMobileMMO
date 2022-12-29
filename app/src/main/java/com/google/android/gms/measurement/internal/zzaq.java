package com.google.android.gms.measurement.internal;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/* compiled from: com.google.android.gms:play-services-measurement-impl@@21.1.1 */
/* loaded from: classes2.dex */
public final class zzaq extends zzgs {
    private long zza;
    private String zzb;
    private AccountManager zzc;
    private Boolean zzd;
    private long zze;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzaq(zzfy zzfyVar) {
        super(zzfyVar);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final long zza() {
        zzg();
        return this.zze;
    }

    public final long zzb() {
        zzu();
        return this.zza;
    }

    public final String zzc() {
        zzu();
        return this.zzb;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void zzd() {
        zzg();
        this.zzd = null;
        this.zze = 0L;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final boolean zze() {
        Account[] result;
        zzg();
        long currentTimeMillis = this.zzs.zzav().currentTimeMillis();
        if (currentTimeMillis - this.zze > 86400000) {
            this.zzd = null;
        }
        Boolean bool = this.zzd;
        if (bool == null) {
            if (ContextCompat.checkSelfPermission(this.zzs.zzau(), "android.permission.GET_ACCOUNTS") != 0) {
                this.zzs.zzay().zzm().zza("Permission error checking for dasher/unicorn accounts");
                this.zze = currentTimeMillis;
                this.zzd = false;
                return false;
            }
            if (this.zzc == null) {
                this.zzc = AccountManager.get(this.zzs.zzau());
            }
            try {
                result = this.zzc.getAccountsByTypeAndFeatures("com.google", new String[]{"service_HOSTED"}, null, null).getResult();
            } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                this.zzs.zzay().zzh().zzb("Exception checking account types", e);
            }
            if (result == null || result.length <= 0) {
                Account[] result2 = this.zzc.getAccountsByTypeAndFeatures("com.google", new String[]{"service_uca"}, null, null).getResult();
                if (result2 != null && result2.length > 0) {
                    this.zzd = true;
                    this.zze = currentTimeMillis;
                    return true;
                }
                this.zze = currentTimeMillis;
                this.zzd = false;
                return false;
            }
            this.zzd = true;
            this.zze = currentTimeMillis;
            return true;
        }
        return bool.booleanValue();
    }

    @Override // com.google.android.gms.measurement.internal.zzgs
    protected final boolean zzf() {
        Calendar calendar = Calendar.getInstance();
        this.zza = TimeUnit.MINUTES.convert(calendar.get(15) + calendar.get(16), TimeUnit.MILLISECONDS);
        Locale locale = Locale.getDefault();
        String lowerCase = locale.getLanguage().toLowerCase(Locale.ENGLISH);
        String lowerCase2 = locale.getCountry().toLowerCase(Locale.ENGLISH);
        this.zzb = lowerCase + "-" + lowerCase2;
        return false;
    }
}
