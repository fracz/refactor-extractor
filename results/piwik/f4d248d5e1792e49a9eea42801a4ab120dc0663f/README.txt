commit f4d248d5e1792e49a9eea42801a4ab120dc0663f
Author: benakamoorthi <benaka.moorthi@gmail.com>
Date:   Sat Apr 7 02:30:49 2012 +0000

    Fixes #53. Augmented the log data deletion feature. Added the ability to purge old reports and metrics.

    Other changes:
      * Added the following plugin functions: Piwik_DropTables, Piwik_OptimizeTables, Piwik_DeleteAllRows. Also refactored existing code to use them.
      * Modified graph, tag cloud & datatable templates/views to show a different message if there's no data for a report and if its possible that report was purged.
      * Refactored DbStats API, added getAllTablesStatus method that doesn't modify the SHOW TABLE STATUS result.
      * Deletelogs config options are now stored in the DB.
      * Added task priority support to the TaskScheduler.



    git-svn-id: http://dev.piwik.org/svn/trunk@6174 59fd770c-687e-43c8-a1e3-f5a4ff64c105