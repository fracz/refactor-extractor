commit 89b37f0357da3813274e2c05a2718879b83f4895
Author: epriestley <git@epriestley.com>
Date:   Thu Nov 1 16:53:17 2012 -0700

    Make various Drydock improvements

    Summary:
    Tightens up a bunch of stuff:

      - In `drydock lease`, pull and print logs so the user can see what's happening.
      - Remove `DrydockAllocator`, which was a dumb class that did nothing. Move the tiny amount of logic it held directly to `DrydockLease`.
      - Move `resourceType` from worker task metadata directly to `DrydockLease`. Other things (like the web UI) can be more informative with this information available.
      - Pass leases to `allocateResource()`. We always allocate in response to a lease activation request, and the lease often has vital information. This also allows us to associate logs with leases correctly.

    Test Plan: Ran `drydock lease --type host` and saw it perform a host allocation in EC2.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2015

    Differential Revision: https://secure.phabricator.com/D3870