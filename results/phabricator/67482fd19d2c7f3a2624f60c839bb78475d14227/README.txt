commit 67482fd19d2c7f3a2624f60c839bb78475d14227
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 2 17:12:15 2016 -0700

    Continue modernizing application access to user preferences

    Summary:
    Ref T4103. This is just incremental cleanup:

      - Add "internal" settings, which aren't editable via the UI. They can still do validation and run through the normal pathway. Move a couple settings to use this.
      - Remove `getPreference()` on `PhabricatorUser`, which was a sort of prototype version of `getUserSetting()`.
      - Make `getUserSetting()` validate setting values before returning them, to improve robustness if we change allowable values later.
      - Add a user setting cache, since reading user settings was getting fairly expensive on Calendar.
      - Improve performance of setting validation for timezone setting (don't require building/computing all timezone offsets).
      - Since we have the cache anyway, make the timezone override a little more general in its approach.
      - Move editor stuff to use `getUserSetting()`.

    Test Plan:
      - Changed search scopes.
      - Reconciled local and server timezone settings by ignoring and changing timezones.
      - Changed date/time settings, browsed Calendar, queried date ranges.
      - Verified editor links generate properly in Diffusion.
      - Browsed around with time/date settings looking at timestamps.
      - Grepped for `getPreference()`, nuked all the ones coming off `$user` or `$viewer` that I could find.
      - Changed accessiblity to high-contrast colors.
      - Ran all unit tests.
      - Grepped for removed constants.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T4103

    Differential Revision: https://secure.phabricator.com/D16015