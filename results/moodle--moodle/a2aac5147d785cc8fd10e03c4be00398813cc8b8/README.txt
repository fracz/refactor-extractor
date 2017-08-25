commit a2aac5147d785cc8fd10e03c4be00398813cc8b8
Author: Neill Magill <neill.magill@nottingham.ac.uk>
Date:   Fri Oct 3 10:27:06 2014 +0100

    MDL-46182 Inefficient query during Moodle upgrade on course_section table.

    This part of the code is changing an index on the course_sections table to be unique, for this to happen in the upgrade script the index must be deleted and then the new version added.

    Before this change the following was being done:

    * The index is removed
    * A query to delete records that were not unique on this index is performed
    * The unique version of the index is added

    After this change the following happens:

    * A query to delete records that were not unique on this index is performed
    * The index is removed
    * The unique version of the index is added

    When the original index is present the query uses it, which greatly improves the execution plan.

    Before the change an upgrade on a site with around 387967 course_section records the upgrade would be stuck on the delete query for many hours, after the change the time can be measured in minutes.