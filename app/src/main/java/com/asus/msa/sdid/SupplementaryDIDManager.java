package com.asus.msa.sdid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.asus.msa.SupplementaryDID.IDidAidlInterface;

/* loaded from: classes.dex */
public class SupplementaryDIDManager {
    public static boolean DEBUG = false;
    public static final String TAG = "SupplementaryDIDManager";
    public Context mContext;
    public IDidAidlInterface mDidService;
    public IDIDBinderStatusListener mListener;
    public boolean isBinded = false;
    public ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.asus.msa.sdid.SupplementaryDIDManager.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            boolean unused = SupplementaryDIDManager.DEBUG;
            SupplementaryDIDManager.this.mDidService = IDidAidlInterface.Stub.asInterface(iBinder);
            SupplementaryDIDManager.this.notifyAllListeners(true);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            SupplementaryDIDManager.this.notifyAllListeners(false);
        }
    };

    public SupplementaryDIDManager(Context context) {
        this.mContext = context;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyAllListeners(boolean z) {
        try {
            if (z) {
                this.mListener.onSuccess(this.mDidService);
            } else {
                this.mListener.onError();
            }
        } catch (Exception e) {
            if (DEBUG) {
                String str = "notify did bind status error :" + e.getMessage();
            }
        }
    }

    public void deInit() {
        ServiceConnection serviceConnection;
        Context context;
        try {
            if (!this.isBinded || (serviceConnection = this.mServiceConnection) == null || (context = this.mContext) == null) {
                return;
            }
            boolean z = DEBUG;
            this.isBinded = false;
            context.unbindService(serviceConnection);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void init(IDIDBinderStatusListener iDIDBinderStatusListener) {
        try {
            this.mListener = iDIDBinderStatusListener;
            Intent intent = new Intent("com.asus.msa.action.ACCESS_DID");
            ComponentName componentName = new ComponentName("com.asus.msa.SupplementaryDID", "com.asus.msa.SupplementaryDID.SupplementaryDIDService");
            Intent intent2 = new Intent(intent);
            intent2.setComponent(componentName);
            boolean z = DEBUG;
            this.isBinded = this.mContext.bindService(intent2, this.mServiceConnection, 1);
        } catch (Exception unused) {
            notifyAllListeners(false);
        }
    }

    public void showLog(boolean z) {
        DEBUG = z;
    }
}
