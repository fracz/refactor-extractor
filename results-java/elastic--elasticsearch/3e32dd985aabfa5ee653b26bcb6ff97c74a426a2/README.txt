commit 3e32dd985aabfa5ee653b26bcb6ff97c74a426a2
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Tue Feb 10 08:54:03 2015 +0100

    Recovery: RecoveryState clean up

    To support the `_recovery` API, the recovery process keeps track of current progress in a class called RecoveryState. This class currently have some issues, mostly around concurrency (see #6644 ). This PR cleans it up as well as other issues around it:

    - Make the Index subsection API cleaner:
    - remove redundant information - all calculation is done based on the underlying file map
    - clearer definition of what is what: total files, vs reused files (local files that match the source) vs recovered files (copied over). % based progress is reported based on recovered files only.
    - cleaned up json response to match other API (sadly this breaks the structure). We now properly report human values for dates and other units.
    - Add more robust unit testing
    - Detail flag was passed along as state (it's now a ToXContent param)
    - State lookup during reporting is now always done via the IndexShard , no more fall backs to many other classes.
    - Cleanup APIs around time and move the little computations to the state class as opposed to doing them out of the API

    I also improved error messages out of the REST testing infra for things I run into.

    Closes #6644
    Closes #9811