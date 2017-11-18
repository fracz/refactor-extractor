commit 2ec30697c6cc6812dfaeb9a2e7d1da0443aeb8e6
Author: Cary Clark <cary@android.com>
Date:   Tue Feb 23 10:50:38 2010 -0500

    refactor drawing to support layers

    Drawing elements that appear atop or below layers need to be
    drawn both in the proper order and with the correct canvas to
    respect clipping and the matrix.

    The logic of what to draw is still in WebView.java, but the
    actual drawing calls are now triggered inside the layer code.

    This still draws layers incorrectly after drawing the history;
    will fix this in a future CL.

    Move drawing to WebView.cpp.
    Use inverseScale to simplify viewPort metrics.
    Remove root layer; let WebView.cpp handle it exclusively.

    Requires companion fix in external/webkit.

    http://b/2457316
    http://b/2454127
    http://b/2454149