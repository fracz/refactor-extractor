commit 6f7bb8c91ad3397308c8dc7e80d702d34f9639e7
Author: epriestley <git@epriestley.com>
Date:   Wed Mar 1 17:13:36 2017 -0800

    On workboards, provide all of the supported "create task" forms in the dropdown

    Summary:
    Ref T12314. Ref T6064. Ref T11580. If an install defines several different task create forms (like "Create Plant" and "Create Animal"), allow any of them to be created directly onto a workboard column.

    This is just a general consistency improvement that makes Custom Forms and Workboards work together a bit better. We might do something fancier eventually for T6064 (which wants fewer clicks) and/or T11580 (which wants per-workboard control over forms or defaults).

    Test Plan:
      - Created several different types of tasks directly onto a workboard.
      - Faked just one create form, saw the UI unchanged (except that it respects any renaming).

    {F3492928}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T12314, T11580, T6064

    Differential Revision: https://secure.phabricator.com/D17446