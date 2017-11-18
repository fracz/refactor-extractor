commit 929ca5e49760c3d5af8c7455cd38e29336b5e262
Author: Jake Hamby <jhamby@google.com>
Date:   Tue Oct 11 19:00:29 2011 -0700

    Fix bug in CDMA WDP datagram handling (fixes incoming MMS).

    CDMA WDP datagram handling was refactored to use the same method
    that handles concatenated SMS messages. WDP datagram sequence numbers
    start at 0, but GSM/CDMA concatenated sequence numbers start at 1.
    Changed SMSDispatcher.processMessagePart() to count from 0 when
    handling WDP datagrams.

    Also changed CdmaSMSDispatcher.processCdmaWapPdu() to correctly
    decode segment numbers > 127 (signed byte conversion bug)
    and to reject PDUs with an out-of-range segment number (invalid
    ranges are already rejected for regular concatenated messages).

    Bug: 5433331
    Change-Id: I25c9567769de8edca789c0d1707d4916a4c46885