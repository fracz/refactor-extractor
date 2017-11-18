commit af8e9f4805643f90a9dc0ecfa119e0a860c12f8a
Author: Suchi Amalapurapu <asuchitra@google.com>
Date:   Tue Jan 12 10:17:28 2010 -0800

    app install on sdcard. provide skeleton implementation
    to install an app on sdcard, just resources.
    Add new install path for /asec in installd.
    ignore . when checking for apk path since the sdcard packages id'ed
    by package name.
    Add new -s option to adb shell pm
    Refactor fwd locked from scanMode to ApplicationInfo.
    Add new flag for sd install
    Add new parse flags for fwd locking and installing on sdcard
    New mock api's in PackageManagerService to invoke MountService api's. These
    will be refactored again and so have been wrapped internally.
    Some error codes in PackageManager
    Changes in PackageManagerService to use mPath and mScanPath during installation
    and switch to using PackageParser.Package.applicationInfo attributes for
    source and public resource directories.
    Some known issues that will be addressed later
     using system_uid for now. needs some tinkering with uid and packagesetting creation to use the actual app uid
     error handling from vold not very robust. ignoring lot of things for now
     sending a delayed destroy to delete packages. will revisit later
     revisit temp file creation later. just copy for now