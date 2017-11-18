commit 8e753330168250677f63d07aa6395276c8e097ab
Author: philwo <philwo@google.com>
Date:   Thu Apr 27 20:24:42 2017 +0200

    worker: Small refactoring of RecordingInputStream.

    Prevents it from potentially throwing an UnsupportedEncodingException.

    Part of #2855.

    PiperOrigin-RevId: 154446897