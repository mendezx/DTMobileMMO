package com.google.android.gms.internal.measurement;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-measurement-base@@21.1.1 */
/* loaded from: classes2.dex */
public final class zzly {
    private static final Class zza;
    private static final zzmn zzb;
    private static final zzmn zzc;
    private static final zzmn zzd;

    static {
        Class<?> cls;
        try {
            cls = Class.forName("com.google.protobuf.GeneratedMessage");
        } catch (Throwable unused) {
            cls = null;
        }
        zza = cls;
        zzb = zzab(false);
        zzc = zzab(true);
        zzd = new zzmp();
    }

    public static zzmn zzA() {
        return zzc;
    }

    public static zzmn zzB() {
        return zzd;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object zzC(int i, List list, zzki zzkiVar, Object obj, zzmn zzmnVar) {
        if (zzkiVar == null) {
            return obj;
        }
        if (list instanceof RandomAccess) {
            int size = list.size();
            Object obj2 = obj;
            int i2 = 0;
            for (int i3 = 0; i3 < size; i3++) {
                int intValue = ((Integer) list.get(i3)).intValue();
                if (zzkiVar.zza(intValue)) {
                    if (i3 != i2) {
                        list.set(i2, Integer.valueOf(intValue));
                    }
                    i2++;
                } else {
                    obj2 = zzD(i, intValue, obj2, zzmnVar);
                }
            }
            if (i2 == size) {
                return obj2;
            }
            list.subList(i2, size).clear();
            return obj2;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            int intValue2 = ((Integer) it.next()).intValue();
            if (!zzkiVar.zza(intValue2)) {
                obj = zzD(i, intValue2, obj, zzmnVar);
                it.remove();
            }
        }
        return obj;
    }

    static Object zzD(int i, int i2, Object obj, zzmn zzmnVar) {
        if (obj == null) {
            obj = zzmnVar.zze();
        }
        zzmnVar.zzf(obj, i, i2);
        return obj;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void zzE(zzjr zzjrVar, Object obj, Object obj2) {
        zzjrVar.zza(obj2);
        throw null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void zzF(zzmn zzmnVar, Object obj, Object obj2) {
        zzmnVar.zzh(obj, zzmnVar.zzd(zzmnVar.zzc(obj), zzmnVar.zzc(obj2)));
    }

    public static void zzG(Class cls) {
        Class cls2;
        if (!zzke.class.isAssignableFrom(cls) && (cls2 = zza) != null && !cls2.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Message classes must extend GeneratedMessage or GeneratedMessageLite");
        }
    }

    public static void zzH(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzc(i, list, z);
    }

    public static void zzI(int i, List list, zznf zznfVar) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zze(i, list);
    }

    public static void zzJ(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzg(i, list, z);
    }

    public static void zzK(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzj(i, list, z);
    }

    public static void zzL(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzl(i, list, z);
    }

    public static void zzM(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzn(i, list, z);
    }

    public static void zzN(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzp(i, list, z);
    }

    public static void zzO(int i, List list, zznf zznfVar, zzlw zzlwVar) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i2 = 0; i2 < list.size(); i2++) {
            ((zzjm) zznfVar).zzq(i, list.get(i2), zzlwVar);
        }
    }

    public static void zzP(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzs(i, list, z);
    }

    public static void zzQ(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzu(i, list, z);
    }

    public static void zzR(int i, List list, zznf zznfVar, zzlw zzlwVar) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i2 = 0; i2 < list.size(); i2++) {
            ((zzjm) zznfVar).zzv(i, list.get(i2), zzlwVar);
        }
    }

    public static void zzS(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzx(i, list, z);
    }

    public static void zzT(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzz(i, list, z);
    }

    public static void zzU(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzB(i, list, z);
    }

    public static void zzV(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzD(i, list, z);
    }

    public static void zzW(int i, List list, zznf zznfVar) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzG(i, list);
    }

    public static void zzX(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzI(i, list, z);
    }

    public static void zzY(int i, List list, zznf zznfVar, boolean z) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        zznfVar.zzK(i, list, z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean zzZ(Object obj, Object obj2) {
        if (obj != obj2) {
            return obj != null && obj.equals(obj2);
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zza(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return size * (zzjl.zzA(i << 3) + 1);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void zzaa(zzlg zzlgVar, Object obj, Object obj2, long j) {
        zzmx.zzs(obj, j, zzlg.zzb(zzmx.zzf(obj, j), zzmx.zzf(obj2, j)));
    }

    private static zzmn zzab(boolean z) {
        Class<?> cls;
        try {
            cls = Class.forName("com.google.protobuf.UnknownFieldSetSchema");
        } catch (Throwable unused) {
            cls = null;
        }
        if (cls == null) {
            return null;
        }
        try {
            return (zzmn) cls.getConstructor(Boolean.TYPE).newInstance(Boolean.valueOf(z));
        } catch (Throwable unused2) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzb(List list) {
        return list.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzc(int i, List list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int zzz = size * zzjl.zzz(i);
        for (int i2 = 0; i2 < list.size(); i2++) {
            zzz += zzjl.zzt((zzjd) list.get(i2));
        }
        return zzz;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzd(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return zze(list) + (size * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zze(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzkf) {
            zzkf zzkfVar = (zzkf) list;
            i = 0;
            while (i2 < size) {
                i += zzjl.zzv(zzkfVar.zze(i2));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                i += zzjl.zzv(((Integer) list.get(i2)).intValue());
                i2++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzf(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return size * (zzjl.zzA(i << 3) + 4);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzg(List list) {
        return list.size() * 4;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzh(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return size * (zzjl.zzA(i << 3) + 8);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzi(List list) {
        return list.size() * 8;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzj(int i, List list, zzlw zzlwVar) {
        int size = list.size();
        if (size != 0) {
            int i2 = 0;
            for (int i3 = 0; i3 < size; i3++) {
                i2 += zzjl.zzu(i, (zzll) list.get(i3), zzlwVar);
            }
            return i2;
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzk(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return zzl(list) + (size * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzl(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzkf) {
            zzkf zzkfVar = (zzkf) list;
            i = 0;
            while (i2 < size) {
                i += zzjl.zzv(zzkfVar.zze(i2));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                i += zzjl.zzv(((Integer) list.get(i2)).intValue());
                i2++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzm(int i, List list, boolean z) {
        if (list.size() == 0) {
            return 0;
        }
        return zzn(list) + (list.size() * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzn(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzla) {
            zzla zzlaVar = (zzla) list;
            i = 0;
            while (i2 < size) {
                i += zzjl.zzB(zzlaVar.zza(i2));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                i += zzjl.zzB(((Long) list.get(i2)).longValue());
                i2++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzo(int i, Object obj, zzlw zzlwVar) {
        if (obj instanceof zzkr) {
            int zzA = zzjl.zzA(i << 3);
            int zza2 = ((zzkr) obj).zza();
            return zzA + zzjl.zzA(zza2) + zza2;
        }
        return zzjl.zzA(i << 3) + zzjl.zzx((zzll) obj, zzlwVar);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzp(int i, List list, zzlw zzlwVar) {
        int zzx;
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int zzz = zzjl.zzz(i) * size;
        for (int i2 = 0; i2 < size; i2++) {
            Object obj = list.get(i2);
            if (obj instanceof zzkr) {
                zzx = zzjl.zzw((zzkr) obj);
            } else {
                zzx = zzjl.zzx((zzll) obj, zzlwVar);
            }
            zzz += zzx;
        }
        return zzz;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzq(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return zzr(list) + (size * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzr(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzkf) {
            zzkf zzkfVar = (zzkf) list;
            i = 0;
            while (i2 < size) {
                int zze = zzkfVar.zze(i2);
                i += zzjl.zzA((zze >> 31) ^ (zze + zze));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                int intValue = ((Integer) list.get(i2)).intValue();
                i += zzjl.zzA((intValue >> 31) ^ (intValue + intValue));
                i2++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzs(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return zzt(list) + (size * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzt(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzla) {
            zzla zzlaVar = (zzla) list;
            i = 0;
            while (i2 < size) {
                long zza2 = zzlaVar.zza(i2);
                i += zzjl.zzB((zza2 >> 63) ^ (zza2 + zza2));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                long longValue = ((Long) list.get(i2)).longValue();
                i += zzjl.zzB((longValue >> 63) ^ (longValue + longValue));
                i2++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzu(int i, List list) {
        int zzy;
        int zzy2;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        int zzz = zzjl.zzz(i) * size;
        if (list instanceof zzkt) {
            zzkt zzktVar = (zzkt) list;
            while (i2 < size) {
                Object zzf = zzktVar.zzf(i2);
                if (zzf instanceof zzjd) {
                    zzy2 = zzjl.zzt((zzjd) zzf);
                } else {
                    zzy2 = zzjl.zzy((String) zzf);
                }
                zzz += zzy2;
                i2++;
            }
        } else {
            while (i2 < size) {
                Object obj = list.get(i2);
                if (obj instanceof zzjd) {
                    zzy = zzjl.zzt((zzjd) obj);
                } else {
                    zzy = zzjl.zzy((String) obj);
                }
                zzz += zzy;
                i2++;
            }
        }
        return zzz;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzv(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return zzw(list) + (size * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzw(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzkf) {
            zzkf zzkfVar = (zzkf) list;
            i = 0;
            while (i2 < size) {
                i += zzjl.zzA(zzkfVar.zze(i2));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                i += zzjl.zzA(((Integer) list.get(i2)).intValue());
                i2++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzx(int i, List list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return zzy(list) + (size * zzjl.zzz(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int zzy(List list) {
        int i;
        int size = list.size();
        int i2 = 0;
        if (size == 0) {
            return 0;
        }
        if (list instanceof zzla) {
            zzla zzlaVar = (zzla) list;
            i = 0;
            while (i2 < size) {
                i += zzjl.zzB(zzlaVar.zza(i2));
                i2++;
            }
        } else {
            i = 0;
            while (i2 < size) {
                i += zzjl.zzB(((Long) list.get(i2)).longValue());
                i2++;
            }
        }
        return i;
    }

    public static zzmn zzz() {
        return zzb;
    }
}
