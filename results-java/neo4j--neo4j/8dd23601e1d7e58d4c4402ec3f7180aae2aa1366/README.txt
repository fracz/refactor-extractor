commit 8dd23601e1d7e58d4c4402ec3f7180aae2aa1366
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Tue Oct 28 16:30:58 2014 +0100

    Cleanup and improvements in IndexStatisticsTest

    - improve assertions in IndexStatisticsTest to be more precise
    - fix tests failures in CI
    - add UpdatesTracker to maintain counts of updates happened during or after population
    - add a test for heavy concurrent updates during population
    - extend IndexingService.Monitor to get a notification when an index has been populated