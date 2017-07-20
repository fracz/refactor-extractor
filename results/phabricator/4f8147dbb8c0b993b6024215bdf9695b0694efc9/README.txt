commit 4f8147dbb8c0b993b6024215bdf9695b0694efc9
Author: epriestley <git@epriestley.com>
Date:   Tue Mar 24 18:49:01 2015 -0700

    Improve protection against SSRF attacks

    Summary:
    Ref T6755. This improves our resistance to SSRF attacks:

      - Follow redirects manually and verify each component of the redirect chain.
      - Handle authentication provider profile picture fetches more strictly.

    Test Plan:
      - Tried to download macros from various URIs which issued redirects, etc.
      - Downloaded an actual macro.
      - Went through external account workflow.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T6755

    Differential Revision: https://secure.phabricator.com/D12151