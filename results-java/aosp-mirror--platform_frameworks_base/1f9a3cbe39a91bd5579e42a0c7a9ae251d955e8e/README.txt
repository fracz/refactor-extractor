commit 1f9a3cbe39a91bd5579e42a0c7a9ae251d955e8e
Author: Yohei Yukawa <yukawa@google.com>
Date:   Thu Oct 26 15:00:59 2017 -0700

    IMMS should preserve enabled/selected IMEs upon boot

    This is a follow up CL to my refactoring CL [1], which unintentionally
    changed the condition to reset default enabled IMEs when the device
    boots up.

    Previously, InputMethodManagerService (IMMS) resets default enabled
    IMEs upon device boot only for the first boot scenario, by checking
    whether Settings.Secure.DEFAULT_INPUT_METHOD is already set or not.

    My refactoring CL accidentally replaced that check with unconditional
    "true", which means now IMMS always resets default enabled IMEs every
    time the device boots up.  This behavior change is of course
    unintentional but has no effect after the user unlocks the device
    because IMMS also discard all the settings change made while
    UserManager.isUserUnlocked() returns false [2].  Hence the above
    behavior change is completely hidden on direct-boot disabled devices,
    where the system behaves as if the user unlocked the device
    immediately after the boot is completed.  The behavior change is
    observable only on direct-boot devices.

    Anyway, IMMS should try to do its best to keep the last used IME and
    user selected IMEs even in user locked state as long as those IMEs
    are compatible with direct-boot.  This CL revives the previous
    behavior by adding the same condition check again.

     [1]: I5b37c450db4b25b3e635b6d634293a34eec8b9d4
          7924782c000733b2d7a180701b74988f0154adee
     [2]: Ifa2225070bf8223f8964cf063c86889e312c5e9a
          ed4952ad0f76a70549777472cd9cefcbc8705917

    Fixes: 67093433
    Fixes: 67491290
    Test: Manually verified as follows
          1. Check out AOSP master
          2. Build an OS image for a direct-boot aware device then
             flash it.
          3. Open
                development/samples/SoftKeyboard/AndroidManifest.xml
             then add
                android:directBootAware="true"
             to the IME service.
          4. Open
                development/samples/SoftKeyboard/res/xml/method.xml
             then add
                android:isAsciiCapable="true"
             to "en_US" IME subtype.
          5. tapas SoftKeyboard
          6. make -j
          7. adb install -r $OUT/system/app/SoftKeyboard/SoftKeyboard.apk
          8. Open system settings:
               System -> Languages & input -> Virtual keyboard
                 -> Manage keyboards
          9. Enable Sample Soft keyboard
         10. Disable Android Keyboard (AOSP)
         11. Open system settings:
               Security -> Screen lock -> Password
             then set up a device password "aaaa".
         12. Reboot the device.
         13. Make sure that
              * Sample Soft keyboard is shown in the unlock screen.
              * AOSP Keyboard is not enabled in the unlock screen.
    Change-Id: Id624d577d941245cca944498dc6935eb364865cf