commit c4858a2ba972e86436d629c4d3f18eb49116de14
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Mon Jun 16 10:51:20 2014 -0700

    Switch PackageParser to reference single path.

    It previously kept mPath separate from mScanPath for some very odd
    edge cases around moving apps-on-SD.  This changes it to always use
    a single path, refactors moving to keep separate paths.

    Refactors method names in PackageParser to be clearer about their
    APK-versus-package relationship.

    Beginnings of a split package parser.  Instead of requiring that
    callers check error codes when null, switch to always throwing on
    parse errors, to require that callers deal with the error.  Longer
    term the entire parser should switch to this style, but its too
    pervasive for a simple refactoring.

    Change-Id: If071d8e55e46e56cc201fadfb51cb471713ae973