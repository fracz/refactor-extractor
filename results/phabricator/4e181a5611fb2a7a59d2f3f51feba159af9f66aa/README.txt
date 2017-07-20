commit 4e181a5611fb2a7a59d2f3f51feba159af9f66aa
Author: epriestley <git@epriestley.com>
Date:   Thu Sep 10 19:28:49 2015 -0700

    Clean up browse/history links in Diffusion

    Summary:
    Fixes T9126. In particular:

      - Add "Browse" links to all history views.
      - Use icons to show "Browse" and "History" links, instead of text.
      - Use FontAwesome.
      - Generally standardize handling of these elements.

    This might need a little design attention, but I think it's an improvement overall.

    Test Plan:
      - Viewed repository history.
      - Viewed branch history.
      - Viewed file history.
      - Viewed table of contents on a commit.
      - Viewed merged changes on a merge commit.
      - Viewed a directory containing an external.
      - Viewed a deleted file.

    {F788419}

    {F788420}

    {F788421}

    {F788422}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9126

    Differential Revision: https://secure.phabricator.com/D14096