commit eaf392163c12aa26665cbf9c15076cec6212ed9e
Author: Lee Hinman <lee@writequit.org>
Date:   Fri Jul 25 15:23:37 2014 +0200

    Add translog checksums

    Switches TranslogStreams to check a header in the file to determine the
    translog format, delegating to the version-specific stream.

    Version 1 of the translog format writes a header using Lucene's
    CodecUtil at the beginning of the file and appends a checksum for each
    translog operation written.

    Also refactors much of the translog operations, such as merging
    .hasNext() and .next() in FsChannelSnapshot

    Relates to #6554