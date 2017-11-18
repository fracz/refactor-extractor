commit 85168a14c53fff360d89fe88856ac0072374f434
Author: Dave Roberge <drobe12@gmail.com>
Date:   Sat Jul 29 09:49:38 2017 -0400

    Consider all routes when looking for candidate coalesced connections. (#3462)

    Previously we'd only look at one route at a time. If DNS returned
    results in random order it would lower our chances of finding a
    coalesced connection. Now we consider all routes at once which hopefully
    improves our chances.