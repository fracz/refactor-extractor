package roboguice.provided.fragment;

import android.view.View;

public class FragmentUtil {
    public static final String SUPPORT_PACKAGE = "android.support.v4.app.";
    public static final String NATIVE_PACKAGE = "android.app.";

    public static f nativeFrag = null;
    public static f supportFrag = null;
    public static Class supportActivity = null;
    public static boolean hasNative = false;
    public static boolean hasSupport = false;

    public static interface f<fragType,fragManagerType> {
        public View getView(fragType frag);
        public fragType findFragmentById(fragManagerType fm, int id);
        public fragType findFragmentByTag(fragManagerType fm, String tag);
        public Class fragmentType();
        public Class fragmentManagerType();
    }


    static {
        try {
            nativeFrag = (f) Class.forName("roboguice.provided.fragment.NativeFragmentUtil").newInstance();
            hasNative = true;
        } catch (Exception e) {}

        try {
            supportFrag = (f) Class.forName("roboguice.support.SupportFragmentUtil").newInstance();
            supportActivity = Class.forName(SUPPORT_PACKAGE+"FragmentActivity");
            hasSupport = true;
        } catch (Exception e) {}

    }
}
