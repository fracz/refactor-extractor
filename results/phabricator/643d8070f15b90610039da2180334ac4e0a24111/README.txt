commit 643d8070f15b90610039da2180334ac4e0a24111
Author: epriestley <git@epriestley.com>
Date:   Sat Oct 6 16:21:25 2012 -0700

    Implement improved remarkup assistance panel

    Summary:
      - I made a "?" icon for help/reference.
      - The `<>` icon was slightly too wide so I carved it down to 14x14.
      - All the icons are in `/Phabriactor/remarkup_icon_sources.psd` if you want to tweak anything.
      - Tooltips don't look like the mock but I'll tackle those separately.
      - Removed strikethrough.
      - Removed tag/image/text size for now since they don't have reasonable JS implementations yet.
      - I think everything else is accurate to the mock.

    Test Plan:
    Normal state:

    {F20621, size=full}

    Hover + Click states:

    {F20622, size=full}

    Clicked state:

    {F20620, size=full}

    Reviewers: chad

    Reviewed By: chad

    CC: aran

    Maniphest Tasks: T1848

    Differential Revision: https://secure.phabricator.com/D3650