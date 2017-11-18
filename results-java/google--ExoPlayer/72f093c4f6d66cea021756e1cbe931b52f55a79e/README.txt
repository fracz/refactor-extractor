commit 72f093c4f6d66cea021756e1cbe931b52f55a79e
Author: Oliver Woodman <olly@google.com>
Date:   Wed Nov 25 17:05:20 2015 +0000

    WebVTT parser improvements.

    * Split findNextCueHeader and validateWebVttHeader into static methods.
      This is a step toward WebVTT in HLS, where we'll need to re-use these
      to peek at the top of the WebVTT file (they'll be moved into a util
      class).
    * Made parser robust against bad cue headers + added a test.
    * Removed spurious looking assertion in WebvttSubtitle.