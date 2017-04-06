commit 4eee88f22bc8b19a5cc17ba8280c5f64dfe821f4
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Sat Sep 8 13:51:18 2012 +0200

    [Routing] improve matching performance by using possesive quantifiers when possible (closes #5471)

    My benchmarks showed a performance improvement of 20% when matching routes that make use of possesive quantifiers because it prevents backtracking when it's not needed