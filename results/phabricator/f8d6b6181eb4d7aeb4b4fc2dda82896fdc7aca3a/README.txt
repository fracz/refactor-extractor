commit f8d6b6181eb4d7aeb4b4fc2dda82896fdc7aca3a
Author: epriestley <git@epriestley.com>
Date:   Tue Dec 6 04:16:54 2016 -0800

    Use PhabricatorCachedClassMapQuery when querying object PHID types

    Summary:
    Ref T11954. When we query for Conduit tokens, we load the associated objects (users) by PHID.

    Currently, querying objects by PHID requires us to load every PHIDType class, when we can know which specific classes we actually need (e.g., just `UserPHIDType`, if only user PHIDs are present in the query).

    Use PhabricatorCachedClassMapQuery to reduce the number of classes we load on this pathway.

    Test Plan:
    - Used `ab -n100` to roughly measure a ~5% performance improvement?
    - This measurement feels a little flimsy but the XHProf profile is cleaner, at least.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11954

    Differential Revision: https://secure.phabricator.com/D16997