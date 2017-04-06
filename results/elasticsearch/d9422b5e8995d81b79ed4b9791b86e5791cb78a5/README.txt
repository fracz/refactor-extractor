commit d9422b5e8995d81b79ed4b9791b86e5791cb78a5
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Jan 21 10:36:43 2016 +0100

    Bring back node throttle type which was lost after index setting refactoring

    This has caused some test failures lately especially on window (which is likely caused
    by the rather bad performance of the windows test machines).
    See one failure here:
     http://build-us-00.elastic.co/job/es_core_master_window-2008/2934/

    This fix has now also a unittest that tests this issue separately.