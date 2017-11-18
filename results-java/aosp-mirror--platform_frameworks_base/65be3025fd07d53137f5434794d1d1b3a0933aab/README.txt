commit 65be3025fd07d53137f5434794d1d1b3a0933aab
Author: Felipe Leme <felipeal@google.com>
Date:   Tue Mar 22 14:53:13 2016 -0700

    Refactored NetworkManagerService to support Data Saver.

    Netd provides 2 bandwidth control rules to restrict which uids can use
    metered networks:

    - bw_penalty_box is a blacklist-based firewall chain used to determine
      which uids do not have access to metered interfaces.

    - bw_happy_box is whitelist-based firewall chain used to determine which
      uids have access to metered interfaces.

    Currently, both NetworkManagerService (NMS) and
    NetworkPolicyManagerService (NPMS) uses just the bw_penalty_box rule,
    which makes turning Data Saver mode on / off too slow (since NPMS needs
    to build the bw_penalty_box on demand); this CL adds support for both
    rules on NMS, although NPMS doesn't take advantage of it yet (it will be
    refactored in a separate CL).

    BUG: 27127112
    BUG: 26685616
    Change-Id: Ib954574f7c86269fc9b4cf8ce4ba72ba5878c23d