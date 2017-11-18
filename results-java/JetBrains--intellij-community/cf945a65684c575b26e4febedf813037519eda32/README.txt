commit cf945a65684c575b26e4febedf813037519eda32
Author: Jaime Wren <jwren@google.com>
Date:   Tue May 2 11:35:57 2017 -0700

    Additional line number pattern matching for the Open File Dialog (TreeFileChooserDialog), `#` and `?l=` are now matched covering web source code viewers such as

    Chromium: https://cs.chromium.org/chromium/src/apps/app_restore_service_factory.cc?l=15

    and

    Android: https://android.googlesource.com/device/sample/+/master/apps/LeanbackCustomizer/src/com/google/android/leanbacklauncher/partnercustomizer/PartnerReceiver.java#100

    Some more context/ additional improvement on: https://youtrack.jetbrains.com/issue/IDEA-162418