commit f6e2fcc2eaf5e650237d3c6d79a2de49d2d4111e
Author: Amith Yamasani <yamasani@google.com>
Date:   Thu Jul 10 13:41:55 2014 -0700

    Improvements to Restrictions API

    Convert restrictions provider to a service instead of a receiver.
    Add a way to get pending responses from restrictions provider.
    Add AbstractRestrictionsProvider.
    Add a callback API for responses.

    Removed some constants in RestrictionsManager.
    Added new constants for errors and error codes.
    Much improved javadocs.

    Bug: 16176009

    Change-Id: I838a50fabc80b94b632294b3a55cd5d8092acf55