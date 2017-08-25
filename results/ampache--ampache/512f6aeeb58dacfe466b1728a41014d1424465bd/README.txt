commit 512f6aeeb58dacfe466b1728a41014d1424465bd
Author: Deathcrow <moralapostel@gmail.com>
Date:   Thu May 28 20:50:06 2015 +0200

    Enables Ogg (also works with Opus) channel streaming
    - bin/channel_run.inc:
            * extensive rewrite and restructuring
            * implements a variable-size server-side buffer
            * small bugfixes here and there
            * allow for more precise bitrate handling (via $nb_chunks_remainder)
            * small improvements to http serving

    - lib/class/channel.class.php:
            * add $header_chunk variable and remember header of current filer for later use in channel streaming
            * added $chunk_size variable