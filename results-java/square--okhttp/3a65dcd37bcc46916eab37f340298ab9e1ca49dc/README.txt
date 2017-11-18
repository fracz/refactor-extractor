commit 3a65dcd37bcc46916eab37f340298ab9e1ca49dc
Author: Neil Fuller <nfuller@google.com>
Date:   Thu Jan 8 15:23:01 2015 +0000

    Fix ResponseCache handling for Android usecases

    Including the code in
    okhttp-android-support/src/test/java/com/squareup/okhttp/android
    provides easy verification of API or behavior changes that might
    affect Android.

    There are some small JavaApiConverter improvements.