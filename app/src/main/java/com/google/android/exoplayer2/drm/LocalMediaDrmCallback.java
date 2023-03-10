package com.google.android.exoplayer2.drm;

import android.support.annotation.Nullable;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;
import java.util.UUID;

/* loaded from: classes.dex */
public final class LocalMediaDrmCallback implements MediaDrmCallback {
    private final byte[] keyResponse;

    public LocalMediaDrmCallback(byte[] keyResponse) {
        this.keyResponse = (byte[]) Assertions.checkNotNull(keyResponse);
    }

    @Override // com.google.android.exoplayer2.drm.MediaDrmCallback
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override // com.google.android.exoplayer2.drm.MediaDrmCallback
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request, @Nullable String mediaProvidedLicenseServerUrl) throws Exception {
        return this.keyResponse;
    }
}
