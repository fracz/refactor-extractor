commit f3af740bdfc261b1cb25c0799af780d3753d4518
Author: Eric Laurent <elaurent@google.com>
Date:   Tue May 5 00:49:01 2009 -0700

    Fixed issue 1709450: Requirements for CDMA Tone Generator

    Added new tone types for CDMA IS-95 specific tones.
    Automatic selection between IS-95, CEPT and JAPAN version base on operator
    country code for call supervisory tones.
    Also improved tone generator capabilities:
    - Each tone segment can now generate its own set of frequencies
    - A tone does not have to be a succession of alternating ON/OFF segments
    - The sequence repetition does not have to start from first segment