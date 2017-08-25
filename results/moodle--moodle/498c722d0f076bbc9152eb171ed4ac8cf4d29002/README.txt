commit 498c722d0f076bbc9152eb171ed4ac8cf4d29002
Author: David Mudrak <david@moodle.com>
Date:   Mon Jul 11 12:12:38 2011 +0200

    MDL-28221 resource: improved file not found handling

    When the referenced file is not found, do not rely on the record in
    resource_old as the resource can be actually restored from a 1.9 backup.