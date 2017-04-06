commit 13ef7d73b9e579bf4c83ff77d6a04c720b04258f
Author: Igor Motov <igor@motovs.org>
Date:   Sat Jan 24 19:59:12 2015 -0500

    Snapshot/Restore: better handling of index deletion during snapshot

    If an index is deleted during initial state of the snapshot operation, the entire snapshot can fail with NPE. This commit improves handling of this situation and allows snapshot to continue if partial snapshots are allowed.

    Closes #9024