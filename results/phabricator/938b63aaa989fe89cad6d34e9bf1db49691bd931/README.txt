commit 938b63aaa989fe89cad6d34e9bf1db49691bd931
Author: epriestley <git@epriestley.com>
Date:   Wed Aug 14 09:40:42 2013 -0700

    Simplify and improve PhabricatorCustomField APIs

    Summary:
    Ref T1703. Ref T3718. The `PhabricatorCustomFieldList` seems like a pretty good idea. Move more code into it to make it harder to get wrong.

    Also the sequencing on old/new values for these transactions was a bit off; fix that up.

    Test Plan: Edited standard and custom profile fields.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1703, T3718

    Differential Revision: https://secure.phabricator.com/D6751