commit 7e0ce081543538318e0b34c1e3700e569de44673
Author: epriestley <git@epriestley.com>
Date:   Tue Nov 6 15:30:11 2012 -0800

    Make various Drydock CLI/Allocator improvements

    Summary:
      - Remove EC2, RemoteHost, Application, etc., blueprints for now. They're very proof-of-concept and Blueprints are getting API changes I don't want to bother propagating for now. Leave the abstract base class and the LocalHost blueprint. I'll restore the more complicated ones once better foundations are in place.
      - Remove the Allocate controller from the web UI. The original vision here was that you'd manually allocate resources in some cases, but it no longer makes sense to do so as all allocations come from leases now. This simplifies allocations and makes the rule for when we can clean up resources clear-cut (if a resource has no more active leases, it can be cleaned up). Instead, we'll build resources like the localhost and remote hosts lazily, when leases come in for them.
      - Add some configuration to manage the localhost blueprint.
      - Refactor `canAllocateResources()` into `isEnabled()` (for config checks) and `canAllocateMoreResources()` (for quota checks, e.g. too many resources are allocated already).
      - Juggle some signatures to align better with a world where blueprints generally do allocate.
      - Add some more logging and error handling.
      - Fix an issue with log ordering.

    Test Plan: Allocated some localhost leases.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2015

    Differential Revision: https://secure.phabricator.com/D3902