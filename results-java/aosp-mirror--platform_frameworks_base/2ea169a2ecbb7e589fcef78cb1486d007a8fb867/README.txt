commit 2ea169a2ecbb7e589fcef78cb1486d007a8fb867
Author: Seigo Nonaka <nona@google.com>
Date:   Mon May 15 16:25:11 2017 -0700

    Introduce early exit path for non resource path.

    Developer can specify android:fontFamily with three ways, pre-defined
    font family name, e.g. "sans-serif", path to the font file in resource
    directory, e.g. "res/fonts/Roboto-Regular.ttf", or path to the XML font
    family file, e.g. "res/fonts/Roboto.xml".

    Resources.getFont treats font files and XML files but pre-defined family
    name is handled by TextView. Thus, we can early exit if the passed value
    is not likely resource path.

    This improves the inflation performance.
    The score without this patch:
    gfx-avg-frame-time-50: 6.9
    gfx-avg-frame-time-90: 9.4
    gfx-avg-frame-time-95: 10.4
    gfx-avg-frame-time-99: 16.7

    The score with this patch:
    gfx-avg-frame-time-50: 7.0
    gfx-avg-frame-time-90: 8.9
    gfx-avg-frame-time-95: 9.7
    gfx-avg-frame-time-99: 16.5

    Measured on bullhead-userdebug.

    The APCT perf test improves from
    String FontFamily: 200,086 -> 132,561
    File FontFamily  : 199,256 -> 161,843
    XML FontFamily   : 203,681 -> 158,553

    Measured on angler-userdebug.

    Bug: 38232467
    Test: UiBenchmark
    Change-Id: Ia601ae7207ae8c60848c9efdbb9396267a57257c