commit 0eb4da7d8e4503a508b9c42bb9e4002ac178cbca
Author: Michajlo Matijkiw <michajlo@google.com>
Date:   Thu Jul 21 01:01:31 2016 +0000

    Remove Rule's dependence on a RawAttributeMapper instance

    The things Rule needs it for aren't terribly complex. Instead inline
    functionality where sensible, and refactor into static methods where not. This
    reduces each Rule's memory footprint by 38%.

    --
    MOS_MIGRATED_REVID=128011760