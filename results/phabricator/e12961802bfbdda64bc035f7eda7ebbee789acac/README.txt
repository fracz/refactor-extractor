commit e12961802bfbdda64bc035f7eda7ebbee789acac
Author: epriestley <git@epriestley.com>
Date:   Wed May 23 12:55:07 2012 -0700

    Minor improvements to email management interface

    Summary:
      - If you have an unverified primary email, we show a disabled "Primary" button right now in the "Status" column. Instead we should show an enabled "Verify" button, to allow you to re-send the verification email.
      - Sort addresses in a predictable way.

    Test Plan:
      - Added, verified and removed a secondary email address.
      - Resent verification email for primary address.
      - Changed primary address.

    Reviewers: btrahan, csilvers

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1184

    Differential Revision: https://secure.phabricator.com/D2548