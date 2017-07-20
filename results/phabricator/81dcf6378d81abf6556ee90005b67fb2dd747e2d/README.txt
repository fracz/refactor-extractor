commit 81dcf6378d81abf6556ee90005b67fb2dd747e2d
Author: epriestley <git@epriestley.com>
Date:   Mon Dec 23 10:43:49 2013 -0800

    Make `repository pull` install hooks the first time

    Summary:
    Ref T4257. Currently, the pull logic looks like this:

      if (new) {
        create();
      } else {
        if (hosted) {
          install_hooks();
        } else {
          update();
        }
      }

    This means that the first time you run `repository pull`, hooks aren't installed, which makes debugging trickier. Instead, reorganize the logic:

      if (new) {
        create();
      } else {
        if (!hosted) {
          update();
        }
      }

      if (hosted) {
        install_hooks();
      }

    Test Plan: Ran `bin/repository pull` on a new `hg` repo and got hooks installed immediately.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T4257

    Differential Revision: https://secure.phabricator.com/D7818