commit bee112c575c8f5c2b5118873b1834114ca9890d6
Author: epriestley <git@epriestley.com>
Date:   Mon Aug 20 14:13:15 2012 -0700

    Fix various app nav issues

    Summary:
      - Fix width, corresponding to wider sprites.
      - Sprite the "Audit" icon.
      - Mark the meta-application as device-ready.
      - Fix some collapse/expand bugs with the draggable local navs.
      - Add texture to local nav.
      - Darken the application nav to make it more cohesive with the main nav. I think this is an improvement?

    Test Plan: See screenshots.

    Reviewers: chad, btrahan

    Reviewed By: btrahan

    CC: aran, netfoxcity

    Maniphest Tasks: T1569

    Differential Revision: https://secure.phabricator.com/D3338