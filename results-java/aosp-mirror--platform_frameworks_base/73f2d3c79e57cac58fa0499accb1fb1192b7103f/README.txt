commit 73f2d3c79e57cac58fa0499accb1fb1192b7103f
Author: Nick Kralevich <nnk@google.com>
Date:   Thu Apr 4 14:38:13 2013 -0700

    Error on conflicting <uses-permission>

    Don't install packages where we have multiple, conflicting
    <uses-permission> lines for the same permission.

    For example, a package which contains:

    <uses-permission android:name="android.permission.INTERNET" android:required="false" />
    <uses-permission android:name="android.permission.INTERNET" android:required="true" />

    will now fail to install.

    In addition, this change slightly refactors the code, and creates a new
    parseUsesPermission() method.

    Change-Id: I0f4bb8b51dc4a0c5b73458a70f706e19829275d2