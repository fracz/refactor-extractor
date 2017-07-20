commit 635bd1ef07c21ce3d494c89c30b3b6b916c1c9de
Author: Anh Nhan Nguyen <anhnhan@outlook.com>
Date:   Fri Apr 5 08:31:35 2013 -0700

    Hovercard front-end code & stuff

    Summary:
    Refs T1048; Depends on D5557, D5558

    They hover right above your eyes. Try it out at home :D (in that case, don't forget to checkout D5557 and D5558)

    Worth a lot of improvement. But it's great for now after a little bit of styling.

    Scrape load (search current document for all hovercards and pre-load them) and lazy load (e.g. previewing comments which is not covered by scrape load) implemented.

    Added some seemingly useful graceful situations. Too much to the left, too much to the top.

    Test Plan:
    Tested on Ubuntu, Chrome and FF. No Windows yet, since I have it live at no place. Works pretty fine, at least it will appear.

    Could test left graceful fallback. Don't know what happens if you place it too far to the top. I expect either good results or placement about the center of the screen in case of glitches.

    For now, they'll disappear right away once the mouse leaves the object tag. Intended is when leaving both tag and hovercard.

    Reviewers: epriestley, chad, btrahan

    CC: aran, Korvin

    Maniphest Tasks: T1048

    Differential Revision: https://secure.phabricator.com/D5563

    Conflicts:

            src/__celerity_resource_map__.php