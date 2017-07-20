commit 51b77878acf71118e9d4cb769d72ded7bbfbf6fa
Author: diosmosis <benaka.moorthi@gmail.com>
Date:   Sat Mar 16 23:31:25 2013 +0000

    Fixes #3465, refactor row evolution code to fix bug where if no labels are specified and last period has no labels, no data is returned. Refactoring also contains optimization for case where no labels are specified.

    Notes:
      * Simplified DataTableManipulator and derived classes.
      * Allow LabelFilter to use multiple labels. Can be used by specifying array for label query parameter (ie, label[]=...).
      * Removed getFilteredTableFromLabel function from datatable types and add getEmptyClone to DataTable_Array.
      * Added setIdSite to php tracker PiwikTracker.