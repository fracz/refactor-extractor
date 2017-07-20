commit e96cd29efff7ebfcbf765eb2c70a1a434c96bb91
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 18 13:06:01 2015 -0700

    Reduce badness in viewing large files in the Diffusion web UI

    Summary:
    Fixes T8597. Second issue there is that if you look at a huge file in Diffusion (like `/path/to/300MB.pdf`) we pull the whole thing over Conduit upfront, then try to shove it into file storage.

    Instead, pull no more than the chunk limit (normally 4MB) and don't spend more than 10s pulling data.

    If we get 4MB of data and/or time out, just fail with a message in the vein of "this is a really big file".

    Eventually, we could improve this:

      - We can determine the //size// of very large files correctly in at least some VCSes, this just takes a little more work. This would let us show the true filesize, at least.
      - We could eventually stream the data out of the VCS, but we can't stream data over Conduit right now and this is a lot of work.

    This is just "stop crashing".

    Test Plan: Changed limits to 0.01 seconds and 8 bytes and saw reasonable errors. Changed them back and got normal beahvior.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T8597

    Differential Revision: https://secure.phabricator.com/D13348