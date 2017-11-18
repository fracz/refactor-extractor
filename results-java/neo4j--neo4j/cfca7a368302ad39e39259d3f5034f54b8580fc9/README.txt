commit cfca7a368302ad39e39259d3f5034f54b8580fc9
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Oct 29 13:20:12 2015 +0100

    Compute correctly last tx log position fields in metadatastore

    The code in neostore/metadatastore return FIELD_NOT_PRESENT when a
    field is missing. Unfortunately the migration code was expecting an
    exception in such a case. Hence the constructed log position is always
    incorrect, i.e., (-1,-1).  This causes problems if the migrated
    database checkpoint on shutdown and no extra transactions are
    committed.  This has been fixed by this commit.

    This change also improves the code that computes those fields when
    they are not present.