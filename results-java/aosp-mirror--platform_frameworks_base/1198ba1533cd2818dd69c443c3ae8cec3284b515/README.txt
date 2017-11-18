commit 1198ba1533cd2818dd69c443c3ae8cec3284b515
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Fri Sep 15 14:18:57 2017 +0900

    Separate connectivity event buffer for bug reports

    This patch uses the RingBuffer class previously extracted out of
    NetdEventListenerService for buffering connectivity events in two
    independent buffers:
     - the current existing buffer used for metrics reporting
     - a new rolling buffer, used for bug report dumpsys.

    This improves the suefulness of connectivity metrics for bug reports
    by solving these three issues tied to the usage of the existing metrics
    reporting buffer:
     - the buffer is always cleared when metrics reporting happens. If a bug
     report is taken shortly after, there is no past connectivity event
     added to that bug report.
     - the buffer has a max capacity and starts dropping new events when it
     saturates, until metrics reporting happens. When this happens, a bug
     report will not contain recent connectivity events.
     - some types of event are rate limited to avoid flooding the metrics
     buffer. events dropped due to rate limits never appears in the bug
     report, but the new bug report buffer ignores rate limiting.

    Bug: 65164242
    Bug: 65700460
    Test: runtest frameworks-net,
          manually inspecting ouput of $ adb shell dumpsys connmetrics -a

    Change-Id: Ia47e566b0c9a6629a26afb7067d5a8efadc25aef