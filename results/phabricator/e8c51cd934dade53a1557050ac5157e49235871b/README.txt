commit e8c51cd934dade53a1557050ac5157e49235871b
Author: epriestley <git@epriestley.com>
Date:   Tue Aug 19 14:21:32 2014 -0700

    Fix external redirect flagging issue with image thumbnails

    Summary: Fixes T5894. This needs some improvement when we lay in real CDN stuff, but should get all the cases right for now.

    Test Plan: Thumbnails work properly again.

    Reviewers: btrahan, chad

    Reviewed By: chad

    Subscribers: epriestley

    Maniphest Tasks: T5894

    Differential Revision: https://secure.phabricator.com/D10299