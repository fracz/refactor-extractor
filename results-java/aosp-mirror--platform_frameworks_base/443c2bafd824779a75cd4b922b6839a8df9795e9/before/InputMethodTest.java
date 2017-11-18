/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;

import com.android.internal.inputmethod.InputMethodUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;

import java.util.ArrayList;
import java.util.List;

public class InputMethodTest extends InstrumentationTestCase {
    private static final boolean IS_AUX = true;
    private static final boolean IS_DEFAULT = true;
    private static final boolean IS_AUTO = true;
    private static final ArrayList<InputMethodSubtype> NO_SUBTYPE = null;

    @SmallTest
    public void testDefaultEnabledImesWithDefaultVoiceIme() throws Exception {
        final Context context = getInstrumentation().getTargetContext();
        final ArrayList<InputMethodInfo> imis = new ArrayList<InputMethodInfo>();
        imis.add(createDefaultAutoDummyVoiceIme());
        imis.add(createNonDefaultAutoDummyVoiceIme0());
        imis.add(createNonDefaultAutoDummyVoiceIme1());
        imis.add(createNonDefaultDummyVoiceIme2());
        imis.add(createDefaultDummyEnUSKeyboardIme());
        imis.add(createNonDefaultDummyJaJPKeyboardIme());
        imis.add(createNonDefaultDummyJaJPKeyboardImeWithoutSubtypes());
        final ArrayList<InputMethodInfo> enabledImis = InputMethodUtils.getDefaultEnabledImes(
                context, true, imis);
        assertEquals(2, enabledImis.size());
        for (int i = 0; i < enabledImis.size(); ++i) {
            final InputMethodInfo imi = enabledImis.get(0);
            // "DummyDefaultAutoVoiceIme" and "DummyDefaultEnKeyboardIme"
            if (imi.getPackageName().equals("DummyDefaultAutoVoiceIme")
                    || imi.getPackageName().equals("DummyDefaultEnKeyboardIme")) {
                continue;
            } else {
                fail("Invalid enabled subtype.");
            }
        }
    }

    @SmallTest
    public void testDefaultEnabledImesWithOutDefaultVoiceIme() throws Exception {
        final Context context = getInstrumentation().getTargetContext();
        final ArrayList<InputMethodInfo> imis = new ArrayList<InputMethodInfo>();
        imis.add(createNonDefaultAutoDummyVoiceIme0());
        imis.add(createNonDefaultAutoDummyVoiceIme1());
        imis.add(createNonDefaultDummyVoiceIme2());
        imis.add(createDefaultDummyEnUSKeyboardIme());
        imis.add(createNonDefaultDummyJaJPKeyboardIme());
        imis.add(createNonDefaultDummyJaJPKeyboardImeWithoutSubtypes());
        final ArrayList<InputMethodInfo> enabledImis = InputMethodUtils.getDefaultEnabledImes(
                context, true, imis);
        assertEquals(3, enabledImis.size());
        for (int i = 0; i < enabledImis.size(); ++i) {
            final InputMethodInfo imi = enabledImis.get(0);
            // "DummyNonDefaultAutoVoiceIme0", "DummyNonDefaultAutoVoiceIme1" and
            // "DummyDefaultEnKeyboardIme"
            if (imi.getPackageName().equals("DummyNonDefaultAutoVoiceIme0")
                    || imi.getPackageName().equals("DummyNonDefaultAutoVoiceIme1")
                    || imi.getPackageName().equals("DummyDefaultEnKeyboardIme")) {
                continue;
            } else {
                fail("Invalid enabled subtype.");
            }
        }
    }

    @SmallTest
    public void testParcelable() throws Exception {
        final ArrayList<InputMethodInfo> originalList = new ArrayList<InputMethodInfo>();
        originalList.add(createNonDefaultAutoDummyVoiceIme0());
        originalList.add(createNonDefaultAutoDummyVoiceIme1());
        originalList.add(createNonDefaultDummyVoiceIme2());
        originalList.add(createDefaultDummyEnUSKeyboardIme());
        originalList.add(createNonDefaultDummyJaJPKeyboardIme());
        originalList.add(createNonDefaultDummyJaJPKeyboardImeWithoutSubtypes());

        final List<InputMethodInfo> clonedList = cloneViaParcel(originalList);
        assertNotNull(clonedList);
        final List<InputMethodInfo> clonedClonedList = cloneViaParcel(clonedList);
        assertNotNull(clonedClonedList);
        assertEquals(originalList, clonedList);
        assertEquals(clonedList, clonedClonedList);
        assertEquals(originalList.size(), clonedList.size());
        assertEquals(clonedList.size(), clonedClonedList.size());
        for (int imeIndex = 0; imeIndex < originalList.size(); ++imeIndex) {
            verifyEquality(originalList.get(imeIndex), clonedList.get(imeIndex));
            verifyEquality(clonedList.get(imeIndex), clonedClonedList.get(imeIndex));
        }
    }

    private static List<InputMethodInfo> cloneViaParcel(final List<InputMethodInfo> list) {
        Parcel p = null;
        try {
            p = Parcel.obtain();
            p.writeTypedList(list);
            p.setDataPosition(0);
            return p.createTypedArrayList(InputMethodInfo.CREATOR);
        } finally {
            if (p != null) {
                p.recycle();
            }
        }
    }

    private static void verifyEquality(InputMethodInfo expected, InputMethodInfo actual) {
        assertEquals(expected, actual);
        assertEquals(expected.getSubtypeCount(), actual.getSubtypeCount());
        for (int subtypeIndex = 0; subtypeIndex < expected.getSubtypeCount(); ++subtypeIndex) {
            final InputMethodSubtype expectedSubtype = expected.getSubtypeAt(subtypeIndex);
            final InputMethodSubtype actualSubtype = actual.getSubtypeAt(subtypeIndex);
            assertEquals(expectedSubtype, actualSubtype);
            assertEquals(expectedSubtype.hashCode(), actualSubtype.hashCode());
        }
    }

    private static InputMethodInfo createDummyInputMethodInfo(String packageName, String name,
            CharSequence label, boolean isAuxIme, boolean isDefault,
            List<InputMethodSubtype> subtypes) {
        final ResolveInfo ri = new ResolveInfo();
        final ServiceInfo si = new ServiceInfo();
        final ApplicationInfo ai = new ApplicationInfo();
        ai.packageName = packageName;
        ai.enabled = true;
        ai.flags |= ApplicationInfo.FLAG_SYSTEM;
        si.applicationInfo = ai;
        si.enabled = true;
        si.packageName = packageName;
        si.name = name;
        si.exported = true;
        si.nonLocalizedLabel = label;
        ri.serviceInfo = si;
        return new InputMethodInfo(ri, isAuxIme, "", subtypes, 1, isDefault);
    }

    private static InputMethodSubtype createDummyInputMethodSubtype(String locale, String mode,
            boolean isAuxiliary, boolean overridesImplicitlyEnabledSubtype) {
        return new InputMethodSubtype(0, 0, locale, mode, "", isAuxiliary,
                overridesImplicitlyEnabledSubtype);
    }

    private static InputMethodInfo createDefaultAutoDummyVoiceIme() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        subtypes.add(createDummyInputMethodSubtype("auto", "voice", IS_AUX, IS_AUTO));
        subtypes.add(createDummyInputMethodSubtype("en_US", "voice", IS_AUX, !IS_AUTO));
        return createDummyInputMethodInfo("DummyDefaultAutoVoiceIme", "dummy.voice0",
                "DummyVoice0", IS_AUX, IS_DEFAULT, subtypes);
    }

    private static InputMethodInfo createNonDefaultAutoDummyVoiceIme0() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        subtypes.add(createDummyInputMethodSubtype("auto", "voice", IS_AUX, IS_AUTO));
        subtypes.add(createDummyInputMethodSubtype("en_US", "voice", IS_AUX, !IS_AUTO));
        return createDummyInputMethodInfo("DummyNonDefaultAutoVoiceIme0", "dummy.voice1",
                "DummyVoice1", IS_AUX, !IS_DEFAULT, subtypes);
    }

    private static InputMethodInfo createNonDefaultAutoDummyVoiceIme1() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        subtypes.add(createDummyInputMethodSubtype("auto", "voice", IS_AUX, IS_AUTO));
        subtypes.add(createDummyInputMethodSubtype("en_US", "voice", IS_AUX, !IS_AUTO));
        return createDummyInputMethodInfo("DummyNonDefaultAutoVoiceIme1", "dummy.voice2",
                "DummyVoice2", IS_AUX, !IS_DEFAULT, subtypes);
    }

    private static InputMethodInfo createNonDefaultDummyVoiceIme2() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        subtypes.add(createDummyInputMethodSubtype("en_US", "voice", IS_AUX, !IS_AUTO));
        return createDummyInputMethodInfo("DummyNonDefaultVoiceIme2", "dummy.voice3",
                "DummyVoice3", IS_AUX, !IS_DEFAULT, subtypes);
    }

    private static InputMethodInfo createDefaultDummyEnUSKeyboardIme() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        subtypes.add(createDummyInputMethodSubtype("en_US", "keyboard", !IS_AUX, !IS_AUTO));
        return createDummyInputMethodInfo("DummyDefaultEnKeyboardIme", "dummy.keyboard0",
                "DummyKeyboard0", !IS_AUX, IS_DEFAULT, subtypes);
    }

    private static InputMethodInfo createNonDefaultDummyJaJPKeyboardIme() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        subtypes.add(createDummyInputMethodSubtype("ja_JP", "keyboard", !IS_AUX, !IS_AUTO));
        return createDummyInputMethodInfo("DummyNonDefaultJaJPKeyboardIme", "dummy.keyboard1",
                "DummyKeyboard1", !IS_AUX, !IS_DEFAULT, subtypes);
    }

    // Although IMEs that have no subtype are considered to be deprecated, the Android framework
    // must still be able to handle such IMEs as well as IMEs that have at least one subtype.
    private static InputMethodInfo createNonDefaultDummyJaJPKeyboardImeWithoutSubtypes() {
        final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        return createDummyInputMethodInfo("DummyNonDefaultJaJPKeyboardImeWithoutSubtypes",
                "dummy.keyboard2", "DummyKeyboard2", !IS_AUX, !IS_DEFAULT, NO_SUBTYPE);
    }
}