package android.support.v4.view.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/* loaded from: classes.dex */
class AccessibilityNodeInfoCompatApi21 {
    AccessibilityNodeInfoCompatApi21() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<Object> getActionList(Object info) {
        List result = ((AccessibilityNodeInfo) info).getActionList();
        return result;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void addAction(Object info, Object action) {
        ((AccessibilityNodeInfo) info).addAction((AccessibilityNodeInfo.AccessibilityAction) action);
    }

    public static boolean removeAction(Object info, Object action) {
        return ((AccessibilityNodeInfo) info).removeAction((AccessibilityNodeInfo.AccessibilityAction) action);
    }

    public static Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
        return AccessibilityNodeInfo.CollectionInfo.obtain(rowCount, columnCount, hierarchical, selectionMode);
    }

    public static Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
        return AccessibilityNodeInfo.CollectionItemInfo.obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
    }

    public static CharSequence getError(Object info) {
        return ((AccessibilityNodeInfo) info).getError();
    }

    public static void setError(Object info, CharSequence error) {
        ((AccessibilityNodeInfo) info).setError(error);
    }

    public static void setMaxTextLength(Object info, int max) {
        ((AccessibilityNodeInfo) info).setMaxTextLength(max);
    }

    public static int getMaxTextLength(Object info) {
        return ((AccessibilityNodeInfo) info).getMaxTextLength();
    }

    public static Object getWindow(Object info) {
        return ((AccessibilityNodeInfo) info).getWindow();
    }

    public static boolean removeChild(Object info, View child) {
        return ((AccessibilityNodeInfo) info).removeChild(child);
    }

    public static boolean removeChild(Object info, View root, int virtualDescendantId) {
        return ((AccessibilityNodeInfo) info).removeChild(root, virtualDescendantId);
    }

    /* loaded from: classes.dex */
    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        public static boolean isSelected(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).isSelected();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object newAccessibilityAction(int actionId, CharSequence label) {
        return new AccessibilityNodeInfo.AccessibilityAction(actionId, label);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getAccessibilityActionId(Object action) {
        return ((AccessibilityNodeInfo.AccessibilityAction) action).getId();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static CharSequence getAccessibilityActionLabel(Object action) {
        return ((AccessibilityNodeInfo.AccessibilityAction) action).getLabel();
    }
}
