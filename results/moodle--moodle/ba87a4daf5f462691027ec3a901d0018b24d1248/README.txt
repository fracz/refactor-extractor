commit ba87a4daf5f462691027ec3a901d0018b24d1248
Author: mjollnir_ <mjollnir_>
Date:   Wed Nov 17 06:57:28 2004 +0000

    Merged from MOODLE_14_STABLE:

    Fixes to fix_course_sortorder() and course/category.php pages. (martinlanghoff)

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-160
      Minor fix: moodle would crash on high number of courses when doing course creation -- should be more scalable now

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-164
    Fix a bug I have introduced in fix_course_sortorder() - v2

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-210
      Fixed nested transaction in fix_course_sortorder()

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-215
      Performance and memory usage fixes for re-sort courses function

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-268
      Major performance and correctness improvements in the functions that move courses up and down, reorder by name, and in fix_course_sortorder(). All now assume course-sortorder is unique (this is enforced at the DB)

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-332
      fix_coursesortorder() bugfixes and logic simplification

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-333
      courses listing enhancements: bugfix on re-ordering, keep the right page on actions