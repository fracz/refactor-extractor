commit d9296638cdd4ac98882850ed742adbb08856bef2
Author: epriestley <git@epriestley.com>
Date:   Fri Jul 27 17:21:33 2012 -0700

    Fix a "user@domain:path" protocol handling bug

    Summary:
    In D3063, we stopped converting "user@domain:path" git-style URIs, but this broke the SSH-detection code and I missed that in my test plan because my test case uses natural SSH keys so the omission of SSH flags didn't cause failures.

    This code is a bit of a mess anyway. Consolidate and refactor it to be a bit simpler, and add test coverage.

    Test Plan: Ran unit tests. Ran "test_connection.php" in SSH and non-SSH modes, verified SSH modes generated appropriate ssh-agent commands around the git remote commands.

    Reviewers: vrana, btrahan, tberman

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1529

    Differential Revision: https://secure.phabricator.com/D3093