package com.google.android.exoplayer2.util;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@TargetApi(17)
/* loaded from: classes.dex */
public final class EGLSurfaceTexture implements SurfaceTexture.OnFrameAvailableListener, Runnable {
    private static final int[] EGL_CONFIG_ATTRIBUTES = {12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 0, 12327, 12344, 12339, 4, 12344};
    private static final int EGL_PROTECTED_CONTENT_EXT = 12992;
    public static final int SECURE_MODE_NONE = 0;
    public static final int SECURE_MODE_PROTECTED_PBUFFER = 2;
    public static final int SECURE_MODE_SURFACELESS_CONTEXT = 1;
    @Nullable
    private EGLContext context;
    @Nullable
    private EGLDisplay display;
    private final Handler handler;
    @Nullable
    private EGLSurface surface;
    @Nullable
    private SurfaceTexture texture;
    private final int[] textureIdHolder = new int[1];

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface SecureMode {
    }

    /* loaded from: classes.dex */
    public static final class GlException extends RuntimeException {
        private GlException(String msg) {
            super(msg);
        }
    }

    public EGLSurfaceTexture(Handler handler) {
        this.handler = handler;
    }

    public void init(int secureMode) {
        this.display = getDefaultDisplay();
        EGLConfig config = chooseEGLConfig(this.display);
        this.context = createEGLContext(this.display, config, secureMode);
        this.surface = createEGLSurface(this.display, config, this.context, secureMode);
        generateTextureIds(this.textureIdHolder);
        this.texture = new SurfaceTexture(this.textureIdHolder[0]);
        this.texture.setOnFrameAvailableListener(this);
    }

    /* JADX WARN: Type inference failed for: r5v0, types: [android.opengl.EGLContext, android.graphics.SurfaceTexture, android.opengl.EGLSurface, android.opengl.EGLDisplay] */
    public void release() {
        this.handler.removeCallbacks(this);
        try {
            if (this.texture != null) {
                this.texture.release();
                GLES20.glDeleteTextures(1, this.textureIdHolder, 0);
            }
        } finally {
            if (this.display != null && !this.display.equals(EGL14.EGL_NO_DISPLAY)) {
                EGL14.eglMakeCurrent(this.display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            }
            if (this.surface != null && !this.surface.equals(EGL14.EGL_NO_SURFACE)) {
                EGL14.eglDestroySurface(this.display, this.surface);
            }
            if (this.context != null) {
                EGL14.eglDestroyContext(this.display, this.context);
            }
            if (Util.SDK_INT >= 19) {
                EGL14.eglReleaseThread();
            }
            this.display = null;
            this.context = null;
            this.surface = null;
            this.texture = null;
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return (SurfaceTexture) Assertions.checkNotNull(this.texture);
    }

    @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.handler.post(this);
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.texture != null) {
            this.texture.updateTexImage();
        }
    }

    private static EGLDisplay getDefaultDisplay() {
        EGLDisplay display = EGL14.eglGetDisplay(0);
        if (display == null) {
            throw new GlException("eglGetDisplay failed");
        }
        int[] version = new int[2];
        boolean eglInitialized = EGL14.eglInitialize(display, version, 0, version, 1);
        if (!eglInitialized) {
            throw new GlException("eglInitialize failed");
        }
        return display;
    }

    private static EGLConfig chooseEGLConfig(EGLDisplay display) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        boolean success = EGL14.eglChooseConfig(display, EGL_CONFIG_ATTRIBUTES, 0, configs, 0, 1, numConfigs, 0);
        if (!success || numConfigs[0] <= 0 || configs[0] == null) {
            throw new GlException(Util.formatInvariant("eglChooseConfig failed: success=%b, numConfigs[0]=%d, configs[0]=%s", Boolean.valueOf(success), Integer.valueOf(numConfigs[0]), configs[0]));
        }
        return configs[0];
    }

    private static EGLContext createEGLContext(EGLDisplay display, EGLConfig config, int secureMode) {
        int[] glAttributes;
        if (secureMode == 0) {
            glAttributes = new int[]{12440, 2, 12344};
        } else {
            glAttributes = new int[]{12440, 2, EGL_PROTECTED_CONTENT_EXT, 1, 12344};
        }
        EGLContext context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, glAttributes, 0);
        if (context == null) {
            throw new GlException("eglCreateContext failed");
        }
        return context;
    }

    private static EGLSurface createEGLSurface(EGLDisplay display, EGLConfig config, EGLContext context, int secureMode) {
        int[] pbufferAttributes;
        EGLSurface surface;
        if (secureMode == 1) {
            surface = EGL14.EGL_NO_SURFACE;
        } else {
            if (secureMode == 2) {
                pbufferAttributes = new int[]{12375, 1, 12374, 1, EGL_PROTECTED_CONTENT_EXT, 1, 12344};
            } else {
                pbufferAttributes = new int[]{12375, 1, 12374, 1, 12344};
            }
            surface = EGL14.eglCreatePbufferSurface(display, config, pbufferAttributes, 0);
            if (surface == null) {
                throw new GlException("eglCreatePbufferSurface failed");
            }
        }
        boolean eglMadeCurrent = EGL14.eglMakeCurrent(display, surface, surface, context);
        if (!eglMadeCurrent) {
            throw new GlException("eglMakeCurrent failed");
        }
        return surface;
    }

    private static void generateTextureIds(int[] textureIdHolder) {
        GLES20.glGenTextures(1, textureIdHolder, 0);
        int errorCode = GLES20.glGetError();
        if (errorCode != 0) {
            throw new GlException("glGenTextures failed. Error: " + Integer.toHexString(errorCode));
        }
    }
}
