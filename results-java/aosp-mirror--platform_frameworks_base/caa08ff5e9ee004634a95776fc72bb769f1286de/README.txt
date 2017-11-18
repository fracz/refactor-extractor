commit caa08ff5e9ee004634a95776fc72bb769f1286de
Author: John Reck <jreck@google.com>
Date:   Fri Oct 7 13:21:36 2016 -0700

    The bigger update to Canvas

    All draw* calls in Canvas are regular JNI
    All draw* calls in DisplayListCanvas are FastNative
    Unifies Canvas JNI on nMethodName naming

    CanvasPerf results before:
    INSTRUMENTATION_STATUS: basicViewGroupDraw_min=12492
    INSTRUMENTATION_STATUS: recordSimpleBitmapView_min=13912

    and after:
    INSTRUMENTATION_STATUS: basicViewGroupDraw_min=11945
    INSTRUMENTATION_STATUS: recordSimpleBitmapView_min=13318

    Test: refactor, makes & boots
    Change-Id: I06000df1d125e17d60c6498865be7a7638a4a13e