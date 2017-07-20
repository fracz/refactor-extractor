commit 9352c76e818adaac7f20cb041f9f6a9d876cf005
Author: epriestley <git@epriestley.com>
Date:   Fri Oct 17 05:01:40 2014 -0700

    Decouple some aspects of request routing and construction

    Summary:
    Ref T5702. This is a forward-looking change which provides some very broad API improvements but does not implement them. In particular:

      - Controllers no longer require `$request` to construct. This is mostly for T5702, directly, but simplifies things in general. Instead, we call `setRequest()` before using a controller. Only a small number of sites activate controllers, so this is less code overall, and more consistent with most constructors not having any parameters or effects.
      - `$request` now offers `getURIData($key, ...)`. This is an alternate way of accessing `$data` which is currently only available on `willProcessRequest(array $data)`. Almost all controllers which implement this method do so in order to read one or two things out of the URI data. Instead, let them just read this data directly when processing the request.
      - Introduce `handleRequest(AphrontRequest $request)` and deprecate (very softly) `processRequest()`. The majority of `processRequest()` calls begin `$request = $this->getRequest()`, which is avoided with the more practical signature.
      - Provide `getViewer()` on `$request`, and a convenience `getViewer()` on `$controller`. This fixes `$viewer = $request->getUser();` into `$viewer = $request->getViewer();`, and converts the `$request + $viewer` two-liner into a single `$this->getViewer()`.

    Test Plan:
      - Browsed around in general.
      - Hit special controllers (redirect, 404).
      - Hit AuditList controller (uses new style).

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T5702

    Differential Revision: https://secure.phabricator.com/D10698