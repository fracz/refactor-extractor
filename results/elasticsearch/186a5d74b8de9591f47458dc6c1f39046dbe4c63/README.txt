commit 186a5d74b8de9591f47458dc6c1f39046dbe4c63
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Sep 1 19:22:13 2016 +0200

    [TEST] improve ClusterStatsIT to better check mem values returned

    Rather than checking that those values are greater than 0, we can sum up the values gotten from all nodes and check that what is returned is that same value.