commit a05ab9640bf44b86334f522fbc634cb64444d976
Author: Aditya Sastry <ganeshaditya1@gmail.com>
Date:   Wed Mar 19 22:10:32 2014 +0530

    Multicolumn sorting feature request(1507)

    This patch completely implements the feature requested and it modifies 4
    existing files and adds one extra Javascript file. The existing system for
    one column sorting has been refactored to be used for multicolumn sort.

    Files messages.php and makegrid.js where modified to add the tool tip.

    File sql.php was modified to add the import statement for the newly added JS
    file.

    The newly added columndelete.js is responsible for shift deleting the columns.
    It gets the url of the link that was shift clicked and removes the name of the
    column which we want to remove from the order by clause.

    Finally, most of the crux of this feature request is implemented through
    changes to the DisplayResult.class.php. 4 methods of this class have been
    refactored to accomodate the required change. getsortparams() which process
    the order by clause text remains the same, just the output parameters have
    been turned into a array for each order by clause column. isInSort() has been
    modified to check multiple order by clauses. The smart ordering feature in
    to form a new function makeURL, which makes the url for multiple sorted table's
    header's button's targets.

    Signed-off-by: Aditya Sastry <ganeshaditya1@gmail.com>