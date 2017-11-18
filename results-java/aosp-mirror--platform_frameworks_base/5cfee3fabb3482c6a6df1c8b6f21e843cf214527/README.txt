commit 5cfee3fabb3482c6a6df1c8b6f21e843cf214527
Author: Brian Carlstrom <bdc@google.com>
Date:   Tue May 31 01:00:15 2011 -0700

    Integrating keystore with keyguard (Part 1 of 4)

    Summary:

    frameworks/base
      keystore rewrite
      keyguard integration with keystore on keyguard entry or keyguard change
      KeyStore API simplification

    packages/apps/Settings
      Removed com.android.credentials.SET_PASSWORD intent support
      Added keyguard requirement for keystore use

    packages/apps/CertInstaller
      Tracking KeyStore API changes
      Fix for NPE in CertInstaller when certificate lacks basic constraints

    packages/apps/KeyChain
      Tracking KeyStore API changes

    Details:

    frameworks/base

       Move keystore from C to C++ while rewriting password
       implementation. Removed global variables. Added many comments.

            cmds/keystore/Android.mk
            cmds/keystore/keystore.h
            cmds/keystore/keystore.c => cmds/keystore/keystore.cpp
            cmds/keystore/keystore_cli.c => cmds/keystore/keystore_cli.cpp

       Changed saveLockPattern and saveLockPassword to notify the keystore
       on changes so that the keystore master key can be reencrypted when
       the keyguard changes.

            core/java/com/android/internal/widget/LockPatternUtils.java

       Changed unlock screens to pass values for keystore unlock or initialization

            policy/src/com/android/internal/policy/impl/PasswordUnlockScreen.java
            policy/src/com/android/internal/policy/impl/PatternUnlockScreen.java

       KeyStore API changes
       - renamed test() to state(), which now return a State enum
       - made APIs with byte[] key arguments private
       - added new KeyStore.isEmpty used to determine if a keyguard is required

            keystore/java/android/security/KeyStore.java

       In addition to tracking KeyStore API changes, added new testIsEmpty
       and improved some existing tests to validate expect values.

            keystore/tests/src/android/security/KeyStoreTest.java

    packages/apps/Settings

        Removing com.android.credentials.SET_PASSWORD intent with the
        removal of the ability to set an explicit keystore password now
        that the keyguard value is used. Changed to ensure keyguard is
        enabled for keystore install or unlock. Cleaned up interwoven
        dialog handing into discrete dialog helper classes.

            AndroidManifest.xml
            src/com/android/settings/CredentialStorage.java

        Remove layout for entering new password

            res/layout/credentials_dialog.xml

        Remove enable credentials checkbox

            res/xml/security_settings_misc.xml
            src/com/android/settings/SecuritySettings.java

        Added ability to specify minimum quality key to ChooseLockGeneric
        Activity. Used by CredentialStorage, but could also be used by
        CryptKeeperSettings. Changed ChooseLockGeneric to understand
        minimum quality for keystore in addition to DPM and device
        encryption.

            src/com/android/settings/ChooseLockGeneric.java

        Changed to use getActivePasswordQuality from
        getKeyguardStoredPasswordQuality based on experience in
        CredentialStorage. Removed bogus class javadoc.

            src/com/android/settings/CryptKeeperSettings.java

        Tracking KeyStore API changes

            src/com/android/settings/vpn/VpnSettings.java
            src/com/android/settings/wifi/WifiSettings.java

       Removing now unused string resources

            res/values-af/strings.xml
            res/values-am/strings.xml
            res/values-ar/strings.xml
            res/values-bg/strings.xml
            res/values-ca/strings.xml
            res/values-cs/strings.xml
            res/values-da/strings.xml
            res/values-de/strings.xml
            res/values-el/strings.xml
            res/values-en-rGB/strings.xml
            res/values-es-rUS/strings.xml
            res/values-es/strings.xml
            res/values-fa/strings.xml
            res/values-fi/strings.xml
            res/values-fr/strings.xml
            res/values-hr/strings.xml
            res/values-hu/strings.xml
            res/values-in/strings.xml
            res/values-it/strings.xml
            res/values-iw/strings.xml
            res/values-ja/strings.xml
            res/values-ko/strings.xml
            res/values-lt/strings.xml
            res/values-lv/strings.xml
            res/values-ms/strings.xml
            res/values-nb/strings.xml
            res/values-nl/strings.xml
            res/values-pl/strings.xml
            res/values-pt-rPT/strings.xml
            res/values-pt/strings.xml
            res/values-rm/strings.xml
            res/values-ro/strings.xml
            res/values-ru/strings.xml
            res/values-sk/strings.xml
            res/values-sl/strings.xml
            res/values-sr/strings.xml
            res/values-sv/strings.xml
            res/values-sw/strings.xml
            res/values-th/strings.xml
            res/values-tl/strings.xml
            res/values-tr/strings.xml
            res/values-uk/strings.xml
            res/values-vi/strings.xml
            res/values-zh-rCN/strings.xml
            res/values-zh-rTW/strings.xml
            res/values-zu/strings.xml
            res/values/strings.xml

    packages/apps/CertInstaller

      Tracking KeyStore API changes
            src/com/android/certinstaller/CertInstaller.java

      Fix for NPE in CertInstaller when certificate lacks basic constraints
            src/com/android/certinstaller/CredentialHelper.java

    packages/apps/KeyChain

      Tracking KeyStore API changes
            src/com/android/keychain/KeyChainActivity.java
            src/com/android/keychain/KeyChainService.java
            support/src/com/android/keychain/tests/support/IKeyChainServiceTestSupport.aidl
            support/src/com/android/keychain/tests/support/KeyChainServiceTestSupport.java
            tests/src/com/android/keychain/tests/KeyChainServiceTest.java

    Change-Id: Ic141fb5d4b43d12fe62cb1e29c7cbd891b4be35d