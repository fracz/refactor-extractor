commit 48fcecffb6468350ae6562a2a5d8717b8e24f4f0
Author: David Mudrak <david@moodle.com>
Date:   Sat Jun 4 14:43:04 2011 +0200

    MDL-27448 Page module conversion fixed and improved

    The embedded files are now converted into the proper file area on the
    conversion. The previous usage of $CFG->wwwroot/file.php was wrong for two
    reasons: (1) the moodle.xml does not (should not) contain these links but
    $@FILEPHP@$ placeholder and (2) even if it did, we could not compare to
    our wwwroot but to the original wwwroot.

    Also fixed the actual list of fields being written into page.xml - the
    previous version included legacy fields (like type, reference and
    friends).