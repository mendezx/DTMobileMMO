package com.appsflyer.internal;

/* loaded from: classes3.dex */
public class a {
    private static int AppsFlyerConversionListener = 0;
    public static final int AppsFlyerInAppPurchaseValidatorListener = 0;
    public static final byte[] AppsFlyerLib = null;
    private static int getSdkVersion = 1;
    public static byte[] onAppOpenAttribution;
    private static Object onAttributionFailure;
    public static byte[] onConversionDataFail;
    private static Object onDeepLinking;
    private static int onValidateInApp;
    private static int onValidateInAppFailure;

    /* JADX WARN: Removed duplicated region for block: B:22:0x0075  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0077  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x008d  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x00b1  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x007b A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:43:0x00b1 -> B:44:0x00b7). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static java.lang.String $$c(short r9, short r10, byte r11) {
        /*
            Method dump skipped, instructions count: 192
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.appsflyer.internal.a.$$c(short, short, byte):java.lang.String");
    }

    public static int AFInAppEventType(Object obj) {
        int i = onValidateInApp;
        int i2 = (i ^ 35) + ((i & 35) << 1);
        getSdkVersion = i2 % 128;
        int i3 = i2 % 2;
        Object obj2 = onDeepLinking;
        int i4 = onValidateInApp + 21;
        getSdkVersion = i4 % 128;
        if (i4 % 2 == 0) {
        }
        try {
            int intValue = ((Integer) Class.forName($$c((short) 938, AppsFlyerLib[40], AppsFlyerLib[450]), true, (ClassLoader) onAttributionFailure).getMethod($$c((short) 400, AppsFlyerLib[15], AppsFlyerLib[92]), Object.class).invoke(obj2, obj)).intValue();
            int i5 = onValidateInApp;
            int i6 = (i5 & 87) + (i5 | 87);
            getSdkVersion = i6 % 128;
            if ((i6 % 2 == 0 ? (char) 6 : 'E') != 6) {
                return intValue;
            }
            Object obj3 = null;
            super.hashCode();
            return intValue;
        } catch (Throwable th) {
            Throwable cause = th.getCause();
            if (cause != null) {
                throw cause;
            }
            throw th;
        }
    }

    public static int AFKeystoreWrapper(int i) {
        int i2 = onValidateInApp + 7;
        getSdkVersion = i2 % 128;
        int i3 = i2 % 2;
        Object obj = onDeepLinking;
        int i4 = (getSdkVersion + 48) - 1;
        onValidateInApp = i4 % 128;
        int i5 = i4 % 2;
        int i6 = onValidateInApp;
        int i7 = (i6 ^ 109) + ((i6 & 109) << 1);
        getSdkVersion = i7 % 128;
        int i8 = i7 % 2;
        try {
            return ((Integer) Class.forName($$c((short) 938, AppsFlyerLib[40], AppsFlyerLib[450]), true, (ClassLoader) onAttributionFailure).getMethod($$c((short) 400, AppsFlyerLib[15], AppsFlyerLib[92]), Integer.TYPE).invoke(obj, Integer.valueOf(i))).intValue();
        } catch (Throwable th) {
            Throwable cause = th.getCause();
            if (cause != null) {
                throw cause;
            }
            throw th;
        }
    }

    public static Object AFKeystoreWrapper(int i, int i2, char c) {
        int i3 = getSdkVersion;
        int i4 = (i3 ^ 99) + ((i3 & 99) << 1);
        onValidateInApp = i4 % 128;
        int i5 = i4 % 2;
        Object obj = onDeepLinking;
        int i6 = onValidateInApp + 33;
        getSdkVersion = i6 % 128;
        if (i6 % 2 == 0) {
        }
        int i7 = (getSdkVersion + 50) - 1;
        onValidateInApp = i7 % 128;
        int i8 = i7 % 2;
        try {
            return Class.forName($$c((short) 938, AppsFlyerLib[40], AppsFlyerLib[450]), true, (ClassLoader) onAttributionFailure).getMethod($$c((short) 214, AppsFlyerLib[15], AppsFlyerLib[66]), Integer.TYPE, Integer.TYPE, Character.TYPE).invoke(obj, Integer.valueOf(i), Integer.valueOf(i2), Character.valueOf(c));
        } catch (Throwable th) {
            Throwable cause = th.getCause();
            if (cause != null) {
                throw cause;
            }
            throw th;
        }
    }

    static void init$0() {
        int i = (onValidateInApp + 2) - 1;
        getSdkVersion = i % 128;
        int i2 = i % 2;
        byte[] bArr = new byte[986];
        System.arraycopy("]??\u008et\t??\u00153????A????\u000f\u0000\u0001??\r\u0001\u001b??????\u0001!??\u0002\r\u0004????\u0002?????\u0000??\r????\u0014??D??????\u000f??\u0004\r??>??A\u0000\t??\u00153??\u000b??\u00079????\u0006??\u000f????\u0003\u0000\r????3??\u0000\u0004\u0003\u0006\u0002??\u000b??\u0001??\n??\u0003\u0006\u00056????E????\u0003\f????\u0000\t??\u00153??\u0005??A????\u0005\u000f#??\u000f\u0000\u0001????\n??\u0003\u0006\u00056????E????\f??\b@??\u0011????\n??\u0001\t??\u00153????A????\u0005\u000f$??\u0000\u0011??0??????\u0001!??\u0002\r\u0004??\u0003????\r??=????\u0006??\u000f???????!??????????5????5??\u000b??\u0000'??\u000e??????\r\u0004??\u001e??\t\u0000????\u0002>????\u0000??\r????\u0014??C??????\u000f??\u0004\r??=??A\u0000\u0002??.????\u0007??/??????\n????)??\u0005\t??\u000f\u0002??1??????\u0003!????\r\u0004??\u0003????\r??=????\u0006??\u000f?????????2????\u0007??\u000b??\u0006??\u0002????\u0003\u0003????\r??=????\u0006??\u000f?????????\u000e!????5??\u000b??\u0000'??\u000e??????\u0002??1??\u000b??\"??????\u0003!??????????-??\u0000+??\u0011???? ??\t\u000b??\u000b\u000b\u0015??\u0017??????O??\u0005??\u0000\n\u0001??????S??\u0007????K\u0015??\u0016??\u0015??\u0014??\u0015??\u0018??\t??\u00153??\u000b??\u00079????\u000b??\u0000'??\u000e????????\n??\u0003\u0006\u00056????\u0000B????\u0000)????\r\u0001????\u0002??/??\u0004\u000f??\u0004\r??\u0019??\u0005??\u0011??\u0002!????\r\u0002??\u0005????????\u0002>????\u0000??\r????\u0014??C??????\u000f??\u0004\r??=??5\t??\u00153????A??????\u0013\u0018??????\u0001!??\u0002\r\u0004????\u000e?? ????\t??\u00153????A????\u0005\u000f$??\u0000\u0011??*??\u0001\u0004??\u0001!??\u0002\r\u0004????\n??\u0003\u0006\u00056????E??????\r??\u0003\u0001\u0001\b????\u0015????\u0010????\u0002>????\u0000??\r????\u0014??C??????\u000f??\u0004\r??=??=??\b\u0002??\u0002??1??\u000b??\u0000'??\u000e????????\u0003??\u00132??\u0003??\u00132????\u0007??\u000f\u0002??.\u0002\u000f????\u0016????\r??\u000b??\u0011\u0019??\u0007??\u0011????)????\f#??\u0007??\b????\u0001??????\u000e????\u0002>????\u0000??\r????\u0014??C??????\u000f??\u0004\r??=??4\t??\u00153????A??\u0007??\f????\u0002??$??\u0003??\u000b??????\u0002??3????\u0004\u0003??\u0001\u000f\u0015????\f??\n??\u0003\u0006\u00056????\u0000B??????\u000b??\r????%????\r\u0004??\u0015????\u0010\u0016??\t??\u00153????A??????\u0007\u0016??\u0001\u0004??\u0001!??\u0002\r\u0004??\u0002??\u0017??\t??\u000f\t??\u00153????A????\n??\u0015??????\u000b??\u0001??\n??\u0003\u0006\u00056??\u000f??\u0004F??????\u0004\u001f??\u000b????\u000f??\f\u001c??????\u0002??+??\u0005??\u000b\b??+??\u0000\u0004\u0003\u0006\u0002??\u000b??\u0001\t??\u00153????A??????\u0007\u0015??\u0013????\u0003\u0000\r???? ????\b\u0018????\u0000\u0003??\t??\u00153????A??????\u0007!????\u0010??\t????\u0005??\u0005-??\u0005\u000f$??\u0000\u0011????\u000e??!??\u000b??\u0000??%??\u0005??\u0002?????\u0000??\r????\u0014??\u0005\u0011??\r??\u000b??\u0011\u0019??\u0007??\u0011????5????\r\u0002??\u0005??\t\u0004??\t??\u00153????A????????\u0001!??\u0002\r\u0004??".getBytes("ISO-8859-1"), 0, bArr, 0, 986);
        AppsFlyerLib = bArr;
        AppsFlyerInAppPurchaseValidatorListener = 3;
        int i3 = onValidateInApp + 91;
        getSdkVersion = i3 % 128;
        if (!(i3 % 2 != 0)) {
            Object[] objArr = null;
            int length = objArr.length;
        }
    }

    private a() {
    }

    /* JADX WARN: Can't wrap try/catch for region: R(49:2|3|(1:5)(1:732)|6|(1:8)(1:731)|9|(2:10|11)|(40:13|(2:717|718)|(38:713|714|(38:18|19|(1:21)(1:710)|22|(1:24)(1:708)|25|(32:27|28|29|30|(1:32)(3:(1:698)(1:704)|699|(2:701|702)(1:703))|(1:34)(3:694|695|696)|35|(2:(1:38)(1:43)|(3:40|41|42))|44|45|46|47|48|49|50|(1:52)(1:692)|53|(1:55)(1:691)|56|(1:58)(1:690)|59|(1:61)(1:689)|62|(1:64)(1:688)|65|(1:67)(1:687)|68|69|(1:71)(1:685)|72|(3:76|(16:81|82|83|(8:(1:86)(1:99)|87|(2:89|90)|92|93|94|95|97)|(9:558|559|560|561|562|563|564|(3:(1:566)(1:675)|567|(1:673)(20:(1:570)(1:672)|571|(1:(1:574)(1:(3:665|(1:667)(1:669)|668)(1:670)))(1:671)|575|576|577|578|579|580|(4:(1:583)(1:601)|584|(6:586|587|588|(1:590)(1:594)|591|592)(2:599|600)|593)|602|603|604|(1:606)(1:654)|607|(3:609|610|611)(3:(1:617)(1:653)|618|(3:(1:621)(1:649)|622|(3:624|625|626)(11:627|628|629|630|631|632|633|634|635|636|615))(6:650|651|652|613|614|615))|612|613|614|615))|674)(1:101)|102|103|104|105|106|107|108|109|110|111|(16:112|113|114|115|116|(5:118|119|120|121|122)(15:515|516|517|518|519|520|521|522|523|524|525|526|527|528|529)|123|124|125|(1:127)(1:513)|128|(37:408|409|410|411|412|413|414|415|416|(8:417|418|419|420|421|422|423|(6:425|426|427|428|(1:430)(1:495)|(3:432|433|434)(1:435))(1:499))|436|437|438|439|440|441|442|443|444|445|446|447|448|449|450|451|452|453|454|455|(1:457)|458|459|460|(1:462)(1:466)|(1:464)|465)(51:130|131|132|(1:134)(1:402)|(1:136)(1:401)|137|(1:139)(1:400)|140|141|142|143|144|(3:(1:146)(1:396)|147|(2:394|395)(7:149|150|151|152|(1:154)(1:393)|155|(4:157|158|159|160)(1:204)))|205|206|207|208|(1:210)(1:392)|211|212|213|214|215|216|217|218|219|220|221|222|223|224|225|226|227|228|229|230|231|232|233|234|235|236|237|238|239|(1:241)(1:362)|242|(5:244|245|246|247|248)|273)|(1:275)(1:361)|276|(13:278|279|280|281|282|283|284|285|286|287|288|289|(1:291))(4:348|349|350|351)|(15:293|294|(1:296)(1:321)|297|298|299|300|301|302|303|304|305|306|307|308)(6:325|326|(1:328)(1:332)|329|330|331)))(2:78|79)|80)|682)|707|30|(0)(0)|(0)(0)|35|(0)|44|45|46|47|48|49|50|(0)(0)|53|(0)(0)|56|(0)(0)|59|(0)(0)|62|(0)(0)|65|(0)(0)|68|69|(0)(0)|72|(4:74|76|(0)(0)|80)|683|684)|712|(0)(0)|25|(0)|707|30|(0)(0)|(0)(0)|35|(0)|44|45|46|47|48|49|50|(0)(0)|53|(0)(0)|56|(0)(0)|59|(0)(0)|62|(0)(0)|65|(0)(0)|68|69|(0)(0)|72|(0)|683|684)|16|(0)|712|(0)(0)|25|(0)|707|30|(0)(0)|(0)(0)|35|(0)|44|45|46|47|48|49|50|(0)(0)|53|(0)(0)|56|(0)(0)|59|(0)(0)|62|(0)(0)|65|(0)(0)|68|69|(0)(0)|72|(0)|683|684)|724|725|726|(0)|16|(0)|712|(0)(0)|25|(0)|707|30|(0)(0)|(0)(0)|35|(0)|44|45|46|47|48|49|50|(0)(0)|53|(0)(0)|56|(0)(0)|59|(0)(0)|62|(0)(0)|65|(0)(0)|68|69|(0)(0)|72|(0)|683|684) */
    /* JADX WARN: Code restructure failed: missing block: B:137:0x049b, code lost:
        if (((java.lang.Boolean) java.lang.Class.forName($$c(r11, com.appsflyer.internal.a.AppsFlyerLib[r13], com.appsflyer.internal.a.AppsFlyerLib[r10])).getMethod($$c((short) 185, com.appsflyer.internal.a.AppsFlyerLib[40], com.appsflyer.internal.a.AppsFlyerLib[62]), r6).invoke(r14, r6)).booleanValue() != false) goto L100;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:102:0x03fc  */
    /* JADX WARN: Removed duplicated region for block: B:103:0x03fe  */
    /* JADX WARN: Removed duplicated region for block: B:106:0x0406  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x0408  */
    /* JADX WARN: Removed duplicated region for block: B:110:0x040f  */
    /* JADX WARN: Removed duplicated region for block: B:111:0x0411  */
    /* JADX WARN: Removed duplicated region for block: B:114:0x041b  */
    /* JADX WARN: Removed duplicated region for block: B:115:0x041d  */
    /* JADX WARN: Removed duplicated region for block: B:119:0x0430  */
    /* JADX WARN: Removed duplicated region for block: B:120:0x0433  */
    /* JADX WARN: Removed duplicated region for block: B:123:0x043b  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x00ff  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x015f  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x0162  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x0168  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x019f  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x01a1  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0201  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0202 A[Catch: Exception -> 0x1b4b, TRY_LEAVE, TryCatch #86 {Exception -> 0x1b4b, blocks: (B:3:0x0010, B:77:0x027e, B:90:0x0322, B:92:0x03ae, B:96:0x03ed, B:104:0x03ff, B:108:0x0409, B:112:0x0412, B:116:0x041e, B:125:0x043f, B:797:0x1a7d, B:799:0x1a80, B:805:0x1a8c, B:817:0x1b10, B:806:0x1a99, B:802:0x1a86, B:68:0x0202, B:56:0x01ac, B:10:0x0030, B:79:0x0295, B:808:0x1ab1, B:809:0x1aea, B:57:0x01cc, B:88:0x02ee, B:70:0x024d, B:69:0x020f), top: B:873:0x0010 }] */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0277  */
    /* JADX WARN: Removed duplicated region for block: B:799:0x1a80 A[Catch: Exception -> 0x1b4b, TryCatch #86 {Exception -> 0x1b4b, blocks: (B:3:0x0010, B:77:0x027e, B:90:0x0322, B:92:0x03ae, B:96:0x03ed, B:104:0x03ff, B:108:0x0409, B:112:0x0412, B:116:0x041e, B:125:0x043f, B:797:0x1a7d, B:799:0x1a80, B:805:0x1a8c, B:817:0x1b10, B:806:0x1a99, B:802:0x1a86, B:68:0x0202, B:56:0x01ac, B:10:0x0030, B:79:0x0295, B:808:0x1ab1, B:809:0x1aea, B:57:0x01cc, B:88:0x02ee, B:70:0x024d, B:69:0x020f), top: B:873:0x0010 }] */
    /* JADX WARN: Removed duplicated region for block: B:805:0x1a8c A[Catch: Exception -> 0x1b4b, TryCatch #86 {Exception -> 0x1b4b, blocks: (B:3:0x0010, B:77:0x027e, B:90:0x0322, B:92:0x03ae, B:96:0x03ed, B:104:0x03ff, B:108:0x0409, B:112:0x0412, B:116:0x041e, B:125:0x043f, B:797:0x1a7d, B:799:0x1a80, B:805:0x1a8c, B:817:0x1b10, B:806:0x1a99, B:802:0x1a86, B:68:0x0202, B:56:0x01ac, B:10:0x0030, B:79:0x0295, B:808:0x1ab1, B:809:0x1aea, B:57:0x01cc, B:88:0x02ee, B:70:0x024d, B:69:0x020f), top: B:873:0x0010 }] */
    /* JADX WARN: Removed duplicated region for block: B:816:0x1af5  */
    /* JADX WARN: Removed duplicated region for block: B:903:0x00d6 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:908:0x0443 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:94:0x03ea  */
    /* JADX WARN: Removed duplicated region for block: B:959:0x1a99 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:95:0x03ec  */
    /* JADX WARN: Removed duplicated region for block: B:967:0x1a89 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:98:0x03f3  */
    /* JADX WARN: Removed duplicated region for block: B:99:0x03f6  */
    /* JADX WARN: Type inference failed for: r21v2 */
    static {
        /*
            Method dump skipped, instructions count: 6997
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.appsflyer.internal.a.<clinit>():void");
    }
}
