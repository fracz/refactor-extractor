commit 1bc039a8ca8a3e17c9f9a2b7966cc3b8c155f617
Author: Max Cai <maxtroy@users.noreply.github.com>
Date:   Thu Apr 20 10:27:36 2017 -0400

    RepoAdapter: allow static items presented with 0 or more item views (#157)

    This commit fixes how stable IDs are calculated for static items. Also:
    - used a different internal structure in RepoAdapter for better readability;
    - polished the code and tests a bit.