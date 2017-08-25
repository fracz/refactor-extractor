commit 2ea2c1f57aba8b59ba4f3a8cb35e3acb1111f267
Author: jamiesensei <jamiesensei>
Date:   Tue Sep 16 09:50:24 2008 +0000

    MDL-16528 "responses report : improve efficiency of sql" moved the querying for state data all into one big query rather than an individual query for each question in each attempt.