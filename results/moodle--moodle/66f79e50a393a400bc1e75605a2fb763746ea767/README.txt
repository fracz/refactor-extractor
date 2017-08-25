commit 66f79e50a393a400bc1e75605a2fb763746ea767
Author: David Mudrak <david@moodle.com>
Date:   Fri May 27 04:00:36 2011 +0200

    Files conversion support improved

    The new file manager class is introduced. Once its public properties are
    set, one can use it to migrate either a single file or a whole
    directory. The course_files conversion reworked so that it uses the new
    manager now. The files.xml written at the very end of the conversion,
    giving all handlers a chance to migrate the files they need.