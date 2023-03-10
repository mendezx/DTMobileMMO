package com.google.android.exoplayer2.upstream.cache;

import com.google.android.exoplayer2.util.Assertions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class ContentMetadataMutations {
    private final Map<String, Object> editedValues = new HashMap();
    private final List<String> removedValues = new ArrayList();

    public ContentMetadataMutations set(String name, String value) {
        return checkAndSet(name, value);
    }

    public ContentMetadataMutations set(String name, long value) {
        return checkAndSet(name, Long.valueOf(value));
    }

    public ContentMetadataMutations set(String name, byte[] value) {
        return checkAndSet(name, Arrays.copyOf(value, value.length));
    }

    public ContentMetadataMutations remove(String name) {
        this.removedValues.add(name);
        this.editedValues.remove(name);
        return this;
    }

    public List<String> getRemovedValues() {
        return Collections.unmodifiableList(new ArrayList(this.removedValues));
    }

    public Map<String, Object> getEditedValues() {
        HashMap<String, Object> hashMap = new HashMap<>(this.editedValues);
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof byte[]) {
                byte[] bytes = (byte[]) value;
                entry.setValue(Arrays.copyOf(bytes, bytes.length));
            }
        }
        return Collections.unmodifiableMap(hashMap);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private ContentMetadataMutations checkAndSet(String name, Object value) {
        this.editedValues.put(Assertions.checkNotNull(name), Assertions.checkNotNull(value));
        this.removedValues.remove(name);
        return this;
    }
}
