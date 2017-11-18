commit 2bf19b493bd8c8b93ea814a79a8a658249ba4ca6
Author: Eugene Kudelevsky <Eugene.Kudelevsky@jetbrains.com>
Date:   Mon Dec 16 16:35:30 2013 +0400

    IDEA-116593 improve choosing of Android and Java SDKs when creating new android-gradle project/module:
    choose only Android SDK location (if we don't know it) on first step, not build target;
    choose Java SDK (if there is no any) separately, set it as project sdk and as internal jdk for newly created android sdk entities