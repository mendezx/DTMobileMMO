package com.google.android.gms.measurement.internal;

import android.content.Context;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.source.chunk.ChunkedTrackBlacklistUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* compiled from: com.google.android.gms:play-services-measurement-impl@@21.1.1 */
/* loaded from: classes2.dex */
public final class zzeb {
    public static final zzea zzA;
    public static final zzea zzB;
    public static final zzea zzC;
    public static final zzea zzD;
    public static final zzea zzE;
    public static final zzea zzF;
    public static final zzea zzG;
    public static final zzea zzH;
    public static final zzea zzI;
    public static final zzea zzJ;
    public static final zzea zzK;
    public static final zzea zzL;
    public static final zzea zzM;
    public static final zzea zzN;
    public static final zzea zzO;
    public static final zzea zzP;
    public static final zzea zzQ;
    public static final zzea zzR;
    public static final zzea zzS;
    public static final zzea zzT;
    public static final zzea zzU;
    public static final zzea zzV;
    public static final zzea zzW;
    public static final zzea zzX;
    public static final zzea zzY;
    public static final zzea zzZ;
    public static final zzea zzaA;
    public static final zzea zzaB;
    public static final zzea zzaa;
    public static final zzea zzab;
    public static final zzea zzac;
    public static final zzea zzad;
    public static final zzea zzae;
    public static final zzea zzaf;
    public static final zzea zzag;
    public static final zzea zzah;
    public static final zzea zzai;
    public static final zzea zzaj;
    public static final zzea zzak;
    public static final zzea zzal;
    public static final zzea zzam;
    public static final zzea zzan;
    public static final zzea zzao;
    public static final zzea zzap;
    public static final zzea zzaq;
    public static final zzea zzar;
    public static final zzea zzas;
    public static final zzea zzat;
    public static final zzea zzau;
    public static final zzea zzav;
    public static final zzea zzaw;
    public static final zzea zzax;
    public static final zzea zzay;
    public static final zzea zzaz;
    public static final zzea zzl;
    public static final zzea zzm;
    public static final zzea zzn;
    public static final zzea zzo;
    public static final zzea zzp;
    public static final zzea zzq;
    public static final zzea zzr;
    public static final zzea zzs;
    public static final zzea zzt;
    public static final zzea zzu;
    public static final zzea zzv;
    public static final zzea zzw;
    public static final zzea zzx;
    public static final zzea zzy;
    public static final zzea zzz;
    private static final List zzaC = Collections.synchronizedList(new ArrayList());
    private static final Set zzaD = Collections.synchronizedSet(new HashSet());
    public static final zzea zza = zza("measurement.ad_id_cache_time", 10000L, 10000L, zzbj.zza);
    public static final zzea zzb = zza("measurement.monitoring.sample_period_millis", 86400000L, 86400000L, zzbb.zza);
    public static final zzea zzc = zza("measurement.config.cache_time", 86400000L, 3600000L, zzbn.zza);
    public static final zzea zzd = zza("measurement.config.url_scheme", "https", "https", zzbz.zza);
    public static final zzea zze = zza("measurement.config.url_authority", "app-measurement.com", "app-measurement.com", zzcl.zza);
    public static final zzea zzf = zza("measurement.upload.max_bundles", 100, 100, zzcx.zza);
    public static final zzea zzg = zza("measurement.upload.max_batch_size", 65536, 65536, zzdj.zza);
    public static final zzea zzh = zza("measurement.upload.max_bundle_size", 65536, 65536, zzdr.zza);
    public static final zzea zzi = zza("measurement.upload.max_events_per_bundle", 1000, 1000, zzds.zza);
    public static final zzea zzj = zza("measurement.upload.max_events_per_day", 100000, 100000, zzdt.zza);
    public static final zzea zzk = zza("measurement.upload.max_error_events_per_day", 1000, 1000, zzbu.zza);

    static {
        Integer valueOf = Integer.valueOf((int) DefaultLoadControl.DEFAULT_MAX_BUFFER_MS);
        zzl = zza("measurement.upload.max_public_events_per_day", valueOf, valueOf, zzcf.zza);
        zzm = zza("measurement.upload.max_conversions_per_day", 10000, 10000, zzcq.zza);
        zzn = zza("measurement.upload.max_realtime_events_per_day", 10, 10, zzdb.zza);
        zzo = zza("measurement.store.max_stored_events_per_app", 100000, 100000, zzdm.zza);
        zzp = zza("measurement.upload.url", "https://app-measurement.com/a", "https://app-measurement.com/a", zzdu.zza);
        zzq = zza("measurement.upload.backoff_period", 43200000L, 43200000L, zzdv.zza);
        zzr = zza("measurement.upload.window_interval", 3600000L, 3600000L, zzdw.zza);
        zzs = zza("measurement.upload.interval", 3600000L, 3600000L, zzaz.zza);
        zzt = zza("measurement.upload.realtime_upload_interval", 10000L, 10000L, zzba.zza);
        zzu = zza("measurement.upload.debug_upload_interval", 1000L, 1000L, zzbc.zza);
        zzv = zza("measurement.upload.minimum_delay", 500L, 500L, zzbd.zza);
        Long valueOf2 = Long.valueOf((long) ChunkedTrackBlacklistUtil.DEFAULT_TRACK_BLACKLIST_MS);
        zzw = zza("measurement.alarm_manager.minimum_interval", valueOf2, valueOf2, zzbe.zza);
        zzx = zza("measurement.upload.stale_data_deletion_interval", 86400000L, 86400000L, zzbf.zza);
        zzy = zza("measurement.upload.refresh_blacklisted_config_interval", 604800000L, 604800000L, zzbg.zza);
        zzz = zza("measurement.upload.initial_upload_delay_time", 15000L, 15000L, zzbh.zza);
        zzA = zza("measurement.upload.retry_time", 1800000L, 1800000L, zzbi.zza);
        zzB = zza("measurement.upload.retry_count", 6, 6, zzbk.zza);
        zzC = zza("measurement.upload.max_queue_time", 2419200000L, 2419200000L, zzbl.zza);
        zzD = zza("measurement.lifetimevalue.max_currency_tracked", 4, 4, zzbm.zza);
        zzE = zza("measurement.audience.filter_result_max_count", 200, 200, zzbo.zza);
        zzF = zza("measurement.upload.max_public_user_properties", 25, 25, null);
        zzG = zza("measurement.upload.max_event_name_cardinality", 500, 500, null);
        zzH = zza("measurement.upload.max_public_event_params", 25, 25, null);
        zzI = zza("measurement.service_client.idle_disconnect_millis", 5000L, 5000L, zzbp.zza);
        zzJ = zza("measurement.test.boolean_flag", false, false, zzbq.zza);
        zzK = zza("measurement.test.string_flag", "---", "---", zzbr.zza);
        zzL = zza("measurement.test.long_flag", -1L, -1L, zzbs.zza);
        zzM = zza("measurement.test.int_flag", -2, -2, zzbt.zza);
        Double valueOf3 = Double.valueOf(-3.0d);
        zzN = zza("measurement.test.double_flag", valueOf3, valueOf3, zzbv.zza);
        zzO = zza("measurement.experiment.max_ids", 50, 50, zzbw.zza);
        zzP = zza("measurement.max_bundles_per_iteration", 100, 100, zzbx.zza);
        zzQ = zza("measurement.sdk.attribution.cache.ttl", 604800000L, 604800000L, zzby.zza);
        zzR = zza("measurement.redaction.app_instance_id.ttl", 7200000L, 7200000L, zzca.zza);
        zzS = zza("measurement.collection.log_event_and_bundle_v2", true, true, zzcb.zza);
        zzT = zza("measurement.quality.checksum", false, false, null);
        zzU = zza("measurement.audience.use_bundle_end_timestamp_for_non_sequence_property_filters", false, false, zzcc.zza);
        zzV = zza("measurement.audience.refresh_event_count_filters_timestamp", false, false, zzcd.zza);
        zzW = zza("measurement.audience.use_bundle_timestamp_for_event_count_filters", false, false, zzce.zza);
        zzX = zza("measurement.sdk.collection.retrieve_deeplink_from_bow_2", true, true, zzcg.zza);
        zzY = zza("measurement.sdk.collection.last_deep_link_referrer_campaign2", false, false, zzch.zza);
        zzZ = zza("measurement.lifecycle.app_in_background_parameter", false, false, zzci.zza);
        zzaa = zza("measurement.integration.disable_firebase_instance_id", false, false, zzcj.zza);
        zzab = zza("measurement.collection.service.update_with_analytics_fix", false, false, zzck.zza);
        zzac = zza("measurement.client.firebase_feature_rollout.v1.enable", true, true, zzcm.zza);
        zzad = zza("measurement.client.sessions.check_on_reset_and_enable2", true, true, zzcn.zza);
        zzae = zza("measurement.collection.synthetic_data_mitigation", false, false, zzco.zza);
        zzaf = zza("measurement.service.storage_consent_support_version", 203600, 203600, zzcp.zza);
        zzag = zza("measurement.client.click_identifier_control.dev", false, false, zzcr.zza);
        zzah = zza("measurement.service.click_identifier_control", false, false, zzcs.zza);
        zzai = zza("measurement.service.store_null_safelist", true, true, zzct.zza);
        zzaj = zza("measurement.service.store_safelist", true, true, zzcu.zza);
        zzak = zza("measurement.redaction.no_aiid_in_config_request", true, true, zzcv.zza);
        zzal = zza("measurement.redaction.config_redacted_fields", true, true, zzcw.zza);
        zzam = zza("measurement.redaction.upload_redacted_fields", true, true, zzcy.zza);
        zzan = zza("measurement.redaction.upload_subdomain_override", true, true, zzcz.zza);
        zzao = zza("measurement.redaction.device_info", true, true, zzda.zza);
        zzap = zza("measurement.redaction.user_id", true, true, zzdc.zza);
        zzaq = zza("measurement.redaction.google_signals", true, true, zzdd.zza);
        zzar = zza("measurement.collection.enable_session_stitching_token.service", false, false, zzde.zza);
        zzas = zza("measurement.collection.enable_session_stitching_token.client.dev", false, false, zzdf.zza);
        zzat = zza("measurement.redaction.app_instance_id", true, true, zzdg.zza);
        zzau = zza("measurement.redaction.populate_ephemeral_app_instance_id", true, true, zzdh.zza);
        zzav = zza("measurement.redaction.enhanced_uid", true, true, zzdi.zza);
        zzaw = zza("measurement.redaction.e_tag", true, true, zzdk.zza);
        zzax = zza("measurement.redaction.client_ephemeral_aiid_generation", true, true, zzdl.zza);
        zzay = zza("measurement.redaction.retain_major_os_version", true, true, zzdn.zza);
        zzaz = zza("measurement.redaction.scion_payload_generator", true, true, zzdo.zza);
        zzaA = zza("measurement.audience.dynamic_filters.oob_fix", true, true, zzdp.zza);
        zzaB = zza("measurement.service.clear_global_params_on_uninstall", true, true, zzdq.zza);
    }

    static zzea zza(String str, Object obj, Object obj2, zzdx zzdxVar) {
        zzea zzeaVar = new zzea(str, obj, obj2, zzdxVar, null);
        zzaC.add(zzeaVar);
        return zzeaVar;
    }

    public static Map zzc(Context context) {
        com.google.android.gms.internal.measurement.zzhe zza2 = com.google.android.gms.internal.measurement.zzhe.zza(context.getContentResolver(), com.google.android.gms.internal.measurement.zzhp.zza("com.google.android.gms.measurement"), zzay.zza);
        return zza2 == null ? Collections.emptyMap() : zza2.zzc();
    }
}
