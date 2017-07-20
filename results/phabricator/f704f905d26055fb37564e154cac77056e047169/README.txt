commit f704f905d26055fb37564e154cac77056e047169
Author: epriestley <git@epriestley.com>
Date:   Mon Jun 19 15:02:22 2017 -0700

    Let PhabricatorSearchCheckboxesField survive saved query data with mismatched types

    Summary:
    Fixes T12851.

    This should fix the error I'm seeing, which is:

    * `Argument 1 passed to array_fuse() must be of the type array, boolean given`

    There may be a better way to patch this up than overriding the getValue() method,
    however.

    Test Plan:
    - Changed the default "Tags" filter to specify `true` instead of `array('self')`, then viewed that filter in the UI.
    - Before patch: fatal.
    - After patch: page loads. Note that `true` is not interpreted as `array('self')`, but the page isn't broken, which is a big improvement.

    Reviewers: #blessed_reviewers, 20after4, chad, amckinley

    Reviewed By: #blessed_reviewers, amckinley

    Subscribers: Korvin

    Maniphest Tasks: T12851

    Differential Revision: https://secure.phabricator.com/D18132