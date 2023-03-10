package com.google.android.exoplayer2.mediacodec;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.media.MediaCodecInfo;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

@TargetApi(16)
/* loaded from: classes.dex */
public final class MediaCodecInfo {
    public static final int MAX_SUPPORTED_INSTANCES_UNKNOWN = -1;
    public static final String TAG = "MediaCodecInfo";
    public final boolean adaptive;
    @Nullable
    public final MediaCodecInfo.CodecCapabilities capabilities;
    @Nullable
    public final String mimeType;
    public final String name;
    public final boolean passthrough;
    public final boolean secure;
    public final boolean tunneling;

    public static MediaCodecInfo newPassthroughInstance(String name) {
        return new MediaCodecInfo(name, null, null, true, false, false);
    }

    public static MediaCodecInfo newInstance(String name, String mimeType, MediaCodecInfo.CodecCapabilities capabilities) {
        return new MediaCodecInfo(name, mimeType, capabilities, false, false, false);
    }

    public static MediaCodecInfo newInstance(String name, String mimeType, MediaCodecInfo.CodecCapabilities capabilities, boolean forceDisableAdaptive, boolean forceSecure) {
        return new MediaCodecInfo(name, mimeType, capabilities, false, forceDisableAdaptive, forceSecure);
    }

    private MediaCodecInfo(String name, @Nullable String mimeType, @Nullable MediaCodecInfo.CodecCapabilities capabilities, boolean passthrough, boolean forceDisableAdaptive, boolean forceSecure) {
        boolean z = false;
        this.name = (String) Assertions.checkNotNull(name);
        this.mimeType = mimeType;
        this.capabilities = capabilities;
        this.passthrough = passthrough;
        this.adaptive = (forceDisableAdaptive || capabilities == null || !isAdaptive(capabilities)) ? false : true;
        this.tunneling = capabilities != null && isTunneling(capabilities);
        if (forceSecure || (capabilities != null && isSecure(capabilities))) {
            z = true;
        }
        this.secure = z;
    }

    public MediaCodecInfo.CodecProfileLevel[] getProfileLevels() {
        return (this.capabilities == null || this.capabilities.profileLevels == null) ? new MediaCodecInfo.CodecProfileLevel[0] : this.capabilities.profileLevels;
    }

    public int getMaxSupportedInstances() {
        if (Util.SDK_INT < 23 || this.capabilities == null) {
            return -1;
        }
        return getMaxSupportedInstancesV23(this.capabilities);
    }

    public boolean isCodecSupported(String codec) {
        MediaCodecInfo.CodecProfileLevel[] profileLevels;
        if (codec == null || this.mimeType == null) {
            return true;
        }
        String codecMimeType = MimeTypes.getMediaMimeType(codec);
        if (codecMimeType == null) {
            return true;
        }
        if (!this.mimeType.equals(codecMimeType)) {
            logNoSupport("codec.mime " + codec + ", " + codecMimeType);
            return false;
        }
        Pair<Integer, Integer> codecProfileAndLevel = MediaCodecUtil.getCodecProfileAndLevel(codec);
        if (codecProfileAndLevel == null) {
            return true;
        }
        for (MediaCodecInfo.CodecProfileLevel capabilities : getProfileLevels()) {
            if (capabilities.profile == ((Integer) codecProfileAndLevel.first).intValue() && capabilities.level >= ((Integer) codecProfileAndLevel.second).intValue()) {
                return true;
            }
        }
        logNoSupport("codec.profileLevel, " + codec + ", " + codecMimeType);
        return false;
    }

    @TargetApi(21)
    public boolean isVideoSizeAndRateSupportedV21(int width, int height, double frameRate) {
        if (this.capabilities == null) {
            logNoSupport("sizeAndRate.caps");
            return false;
        }
        MediaCodecInfo.VideoCapabilities videoCapabilities = this.capabilities.getVideoCapabilities();
        if (videoCapabilities == null) {
            logNoSupport("sizeAndRate.vCaps");
            return false;
        }
        if (!areSizeAndRateSupportedV21(videoCapabilities, width, height, frameRate)) {
            if (width >= height || !areSizeAndRateSupportedV21(videoCapabilities, height, width, frameRate)) {
                logNoSupport("sizeAndRate.support, " + width + "x" + height + "x" + frameRate);
                return false;
            }
            logAssumedSupport("sizeAndRate.rotated, " + width + "x" + height + "x" + frameRate);
        }
        return true;
    }

    @TargetApi(21)
    public Point alignVideoSizeV21(int width, int height) {
        if (this.capabilities == null) {
            logNoSupport("align.caps");
            return null;
        }
        MediaCodecInfo.VideoCapabilities videoCapabilities = this.capabilities.getVideoCapabilities();
        if (videoCapabilities == null) {
            logNoSupport("align.vCaps");
            return null;
        }
        int widthAlignment = videoCapabilities.getWidthAlignment();
        int heightAlignment = videoCapabilities.getHeightAlignment();
        return new Point(Util.ceilDivide(width, widthAlignment) * widthAlignment, Util.ceilDivide(height, heightAlignment) * heightAlignment);
    }

    @TargetApi(21)
    public boolean isAudioSampleRateSupportedV21(int sampleRate) {
        if (this.capabilities == null) {
            logNoSupport("sampleRate.caps");
            return false;
        }
        MediaCodecInfo.AudioCapabilities audioCapabilities = this.capabilities.getAudioCapabilities();
        if (audioCapabilities == null) {
            logNoSupport("sampleRate.aCaps");
            return false;
        } else if (!audioCapabilities.isSampleRateSupported(sampleRate)) {
            logNoSupport("sampleRate.support, " + sampleRate);
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(21)
    public boolean isAudioChannelCountSupportedV21(int channelCount) {
        if (this.capabilities == null) {
            logNoSupport("channelCount.caps");
            return false;
        }
        MediaCodecInfo.AudioCapabilities audioCapabilities = this.capabilities.getAudioCapabilities();
        if (audioCapabilities == null) {
            logNoSupport("channelCount.aCaps");
            return false;
        }
        int maxInputChannelCount = adjustMaxInputChannelCount(this.name, this.mimeType, audioCapabilities.getMaxInputChannelCount());
        if (maxInputChannelCount < channelCount) {
            logNoSupport("channelCount.support, " + channelCount);
            return false;
        }
        return true;
    }

    private void logNoSupport(String message) {
        Log.d(TAG, "NoSupport [" + message + "] [" + this.name + ", " + this.mimeType + "] [" + Util.DEVICE_DEBUG_INFO + "]");
    }

    private void logAssumedSupport(String message) {
        Log.d(TAG, "AssumedSupport [" + message + "] [" + this.name + ", " + this.mimeType + "] [" + Util.DEVICE_DEBUG_INFO + "]");
    }

    private static int adjustMaxInputChannelCount(String name, String mimeType, int maxChannelCount) {
        int assumedMaxChannelCount;
        if (maxChannelCount <= 1) {
            if ((Util.SDK_INT < 26 || maxChannelCount <= 0) && !MimeTypes.AUDIO_MPEG.equals(mimeType) && !MimeTypes.AUDIO_AMR_NB.equals(mimeType) && !MimeTypes.AUDIO_AMR_WB.equals(mimeType) && !MimeTypes.AUDIO_AAC.equals(mimeType) && !MimeTypes.AUDIO_VORBIS.equals(mimeType) && !MimeTypes.AUDIO_OPUS.equals(mimeType) && !MimeTypes.AUDIO_RAW.equals(mimeType) && !MimeTypes.AUDIO_FLAC.equals(mimeType) && !MimeTypes.AUDIO_ALAW.equals(mimeType) && !MimeTypes.AUDIO_MLAW.equals(mimeType) && !MimeTypes.AUDIO_MSGSM.equals(mimeType)) {
                if (MimeTypes.AUDIO_AC3.equals(mimeType)) {
                    assumedMaxChannelCount = 6;
                } else if (MimeTypes.AUDIO_E_AC3.equals(mimeType)) {
                    assumedMaxChannelCount = 16;
                } else {
                    assumedMaxChannelCount = 30;
                }
                Log.w(TAG, "AssumedMaxChannelAdjustment: " + name + ", [" + maxChannelCount + " to " + assumedMaxChannelCount + "]");
                return assumedMaxChannelCount;
            }
            return maxChannelCount;
        }
        return maxChannelCount;
    }

    private static boolean isAdaptive(MediaCodecInfo.CodecCapabilities capabilities) {
        return Util.SDK_INT >= 19 && isAdaptiveV19(capabilities);
    }

    @TargetApi(19)
    private static boolean isAdaptiveV19(MediaCodecInfo.CodecCapabilities capabilities) {
        return capabilities.isFeatureSupported("adaptive-playback");
    }

    private static boolean isTunneling(MediaCodecInfo.CodecCapabilities capabilities) {
        return Util.SDK_INT >= 21 && isTunnelingV21(capabilities);
    }

    @TargetApi(21)
    private static boolean isTunnelingV21(MediaCodecInfo.CodecCapabilities capabilities) {
        return capabilities.isFeatureSupported("tunneled-playback");
    }

    private static boolean isSecure(MediaCodecInfo.CodecCapabilities capabilities) {
        return Util.SDK_INT >= 21 && isSecureV21(capabilities);
    }

    @TargetApi(21)
    private static boolean isSecureV21(MediaCodecInfo.CodecCapabilities capabilities) {
        return capabilities.isFeatureSupported("secure-playback");
    }

    @TargetApi(21)
    private static boolean areSizeAndRateSupportedV21(MediaCodecInfo.VideoCapabilities capabilities, int width, int height, double frameRate) {
        if (frameRate == -1.0d || frameRate <= FirebaseRemoteConfig.DEFAULT_VALUE_FOR_DOUBLE) {
            return capabilities.isSizeSupported(width, height);
        }
        return capabilities.areSizeAndRateSupported(width, height, frameRate);
    }

    @TargetApi(23)
    private static int getMaxSupportedInstancesV23(MediaCodecInfo.CodecCapabilities capabilities) {
        return capabilities.getMaxSupportedInstances();
    }
}
