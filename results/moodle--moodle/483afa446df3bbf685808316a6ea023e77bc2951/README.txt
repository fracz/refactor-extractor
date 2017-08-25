commit 483afa446df3bbf685808316a6ea023e77bc2951
Author: David Mudrák <david@moodle.com>
Date:   Fri Jun 22 15:22:27 2012 +0200

    MDL-33453 Make it clear what search_references() and search_references_count() are good for

    Initially it was not clear enough that these two methods are supposed to
    be used for looking for references to a stored_file only. So the docs
    comments are improved and unittests added to illustrate the usage.

    The patch also removes the unittest for get_references_by_storedfile()
    as its usage is already covered in other test methods.