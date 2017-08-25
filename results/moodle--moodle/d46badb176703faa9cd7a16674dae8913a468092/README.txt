commit d46badb176703faa9cd7a16674dae8913a468092
Author: David Mudrak <david@moodle.com>
Date:   Thu Jun 30 22:40:54 2011 +0200

    MDL-28006 restore - improved handling of gradebook 1.9 backups

    On contrary from a backup created in 2.0, the file gradebook.xml in the
    converted MBZ can contain the course grade_item without the
    corresponding grade_category record which is something restore_stepslib
    did not expect. This patch fixes two places:

    1) the gradebook restore does not use mapped itemid but calls
    grade_category::fetch_course_category() to get the actual category to
    link the course_item with

    2) after_execute makes sure that the mapping actually exists before
    trying to move the activities from the default root category