commit 50f257adee182df5f3e0c3dbae2d044a1e0122cc
Author: epriestley <git@epriestley.com>
Date:   Sun Nov 29 11:28:27 2015 -0800

    Allow EditEngine Conduit endpoints to accept object IDs and monograms

    Summary:
    Ref T9132. This is a quality-of-life improvement for new `application.edit` endpoints.

    Instead of strictly requiring PHIDs, allow IDs or monograms. This primarily makes these endpoints easier to test and use.

    Test Plan: Edited objects via API by passing IDs, PHIDs and monograms.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9132

    Differential Revision: https://secure.phabricator.com/D14600