commit 18b8fbfa0dbaad888cab24eac7d7e1b13521f857
Author: mjollnir_ <mjollnir_>
Date:   Tue Sep 14 22:58:13 2004 +0000

    Centralised file upload code, integration with clam AV, integration with some modules: assignment, exercise, forum, glossaryt, resource, scorm (more to come soon).

    These patches are maintained in an publicly accessible Arch repository, see: http://lists.eduforge.org/cgi-bin/archzoom.cgi/arch-eduforge@catalyst.net.nz--2004-MIRROR/moodle--eduforge--1.3.3

    Index of arch patches in this commit:

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-15
        final touches to sears stuff until testing can begin, beginning of magical uploadey wrappery function goodness

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-18
        Virus scanning on upload

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-19
        made emacs use spaces instead of tabs and fixed lib/moodlelib.php where it was bad in the new functions; few wording changes, added in support for clamdscan

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-20
        handlevirus.php = new script to handle output of clamscan (designed for cron clamscan), changes to strings for emailing out virus notifications, changes to moodlelib - slightly different notice reporting in handle_infected_file and new function for replacing file with message

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-21
        refactor to filter out invalid lines in input to handlevirus

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-22
        modified assignment to use hande_file_upload

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-25
        bug fix for handle_file_upload

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-26
        Small fix for non thinking brain doing something silly

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-35
        small fix to switch order of items in drop down to allow sensible defaults

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-36
        small changes to strings file

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-38
        taken stuff out of moodlelib to put in upload class

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-39
        new upload class -in a changeset by itself just in case - not quite finished

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-40
        tweaks to upload class - clam_scan_file can now take a path as an argument, not just an entry from _FILES, there is better handling of failure and notification, more allowance for module writers to keep control in general. Also slightly nicer strings entries for a few things

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-41
        upload class integration with assignment module, bug fix, slight tweak

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-42
        small changes to uploadlib, integration with assessment and assignment

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-44
        tweaks for assessment and assignment for uploading

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-48
        integration with exercise module

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-49
        integration of virus stuff with forum module

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-50
        integration of upload class and glossary module

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-51
        just in case glossary_move_attachments is ever used, we change the log entries before we move the files. also  moved clam_log_upload out of the class

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-52
        virus scanning for imports for glossary

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-53
        relog entries when moving files attached to forum posts

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-54
        resource module integration with virus scanning

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-55
        scorm integration with upload/virus class

    arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-56
        fix for handlevirus.php since upload class changes


    Full logs:

    Revision: moodle--eduforge--1.3.3--patch-15
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Wed Sep  1 17:28:13 NZST 2004
    Standard-date: 2004-09-01 05:28:13 GMT
    Modified-files: lang/en/moodle.php lib/moodlelib.php
        mod/assessment/sears.php mod/assessment/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-15
    Summary: final touches to sears stuff until testing can begin, beginning of magical uploadey wrappery function goodness
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-18
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Thu Sep  2 15:49:54 NZST 2004
    Standard-date: 2004-09-02 03:49:54 GMT
    Modified-files: admin/config.html lang/en/moodle.php
        lib/moodlelib.php mod/assessment/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-18
    Summary: Virus scanning on upload
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-19
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Thu Sep  2 17:06:14 NZST 2004
    Standard-date: 2004-09-02 05:06:14 GMT
    Modified-files: lang/en/moodle.php lib/moodlelib.php
        mod/assessment/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-19
    Summary: made emacs use spaces instead of tabs and fixed lib/moodlelib.php where it was bad in the new functions; few wording changes, added in support for clamdscan
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-20
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep  3 16:06:05 NZST 2004
    Standard-date: 2004-09-03 04:06:05 GMT
    New-files: admin/.arch-ids/handlevirus.php.id
        admin/handlevirus.php
    Modified-files: lang/en/moodle.php lib/moodlelib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-20
    Summary: handlevirus.php = new script to handle output of clamscan (designed for cron clamscan), changes to strings for emailing out virus notifications, changes to moodlelib - slightly different notice reporting in handle_infected_file and new function for replacing file with message
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-21
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep  6 11:37:31 NZST 2004
    Standard-date: 2004-09-05 23:37:31 GMT
    Modified-files: admin/handlevirus.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-21
    Summary: refactor to filter out invalid lines in input to handlevirus
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-22
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep  6 13:07:48 NZST 2004
    Standard-date: 2004-09-06 01:07:48 GMT
    Modified-files: mod/assignment/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-22
    Summary: modified assignment to use hande_file_upload
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-25
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep  6 16:32:11 NZST 2004
    Standard-date: 2004-09-06 04:32:11 GMT
    Modified-files: lib/moodlelib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-25
    Summary: bug fix for handle_file_upload
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-26
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep  6 16:51:50 NZST 2004
    Standard-date: 2004-09-06 04:51:50 GMT
    Modified-files: lib/moodlelib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-26
    Summary: Small fix for non thinking brain doing something silly
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-35
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 10:09:53 NZST 2004
    Standard-date: 2004-09-09 22:09:53 GMT
    Modified-files: admin/config.html
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-35
    Summary: small fix to switch order of items in drop down to allow sensible defaults
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-36
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 10:11:29 NZST 2004
    Standard-date: 2004-09-09 22:11:29 GMT
    Modified-files: lang/en/moodle.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-36
    Summary: small changes to strings file
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-38
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 10:17:24 NZST 2004
    Standard-date: 2004-09-09 22:17:24 GMT
    Modified-files: lib/moodlelib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-38
    Summary: taken stuff out of moodlelib to put in upload class
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-39
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 10:21:21 NZST 2004
    Standard-date: 2004-09-09 22:21:21 GMT
    New-files: lib/.arch-ids/uploadlib.php.id lib/uploadlib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-39
    Summary: new upload class -in a changeset by itself just in case - not quite finished
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-40
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 11:58:24 NZST 2004
    Standard-date: 2004-09-09 23:58:24 GMT
    Modified-files: lang/en/moodle.php lib/uploadlib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-40
    Summary: tweaks to upload class - clam_scan_file can now take a path as an argument, not just an entry from _FILES, there is better handling of failure and notification, more allowance for module writers to keep control in general. Also slightly nicer strings entries for a few things
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-41
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 12:38:02 NZST 2004
    Standard-date: 2004-09-10 00:38:02 GMT
    Modified-files: lib/uploadlib.php mod/assignment/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-41
    Summary: upload class integration with assignment module, bug fix, slight tweak
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-42
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 15:30:20 NZST 2004
    Standard-date: 2004-09-10 03:30:20 GMT
    Modified-files: lib/uploadlib.php mod/assessment/upload.php
        mod/assessment/view.php mod/assignment/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-42
    Summary: small changes to uploadlib, integration with assessment and assignment
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-44
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Fri Sep 10 16:54:40 NZST 2004
    Standard-date: 2004-09-10 04:54:40 GMT
    Modified-files: mod/assessment/lib.php
        mod/assignment/lib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-44
    Summary: tweaks for assessment and assignment for uploading
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-48
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 09:57:03 NZST 2004
    Standard-date: 2004-09-12 21:57:03 GMT
    Modified-files: lang/en/moodle.php
        mod/exercise/locallib.php mod/exercise/upload.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-48
    Summary: integration with exercise module
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-49
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 11:35:46 NZST 2004
    Standard-date: 2004-09-12 23:35:46 GMT
    Modified-files: mod/forum/lib.php mod/forum/post.html
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-49
    Summary: integration of virus stuff with forum module
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-50
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 14:00:29 NZST 2004
    Standard-date: 2004-09-13 02:00:29 GMT
    Modified-files: lang/en/glossary.php mod/glossary/edit.html
        mod/glossary/edit.php mod/glossary/lib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-50
    Summary: integration of upload class and glossary module
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-51
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 15:13:02 NZST 2004
    Standard-date: 2004-09-13 03:13:02 GMT
    Modified-files: lib/uploadlib.php mod/glossary/lib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-51
    Summary: just in case glossary_move_attachments is ever used, we change the log entries before we move the files. also  moved clam_log_upload out of the class
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-52
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 15:26:56 NZST 2004
    Standard-date: 2004-09-13 03:26:56 GMT
    Modified-files: mod/glossary/import.html
        mod/glossary/import.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-52
    Summary: virus scanning for imports for glossary
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-53
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 16:02:22 NZST 2004
    Standard-date: 2004-09-13 04:02:22 GMT
    Modified-files: mod/forum/lib.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-53
    Summary: relog entries when moving files attached to forum posts
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-54
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Mon Sep 13 16:58:37 NZST 2004
    Standard-date: 2004-09-13 04:58:37 GMT
    Modified-files: mod/resource/coursefiles.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-54
    Summary: resource module integration with virus scanning
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-55
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Tue Sep 14 16:15:47 NZST 2004
    Standard-date: 2004-09-14 04:15:47 GMT
    Modified-files: mod/scorm/coursefiles.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-55
    Summary: scorm integration with upload/virus class
    Keywords:

    Revision: moodle--eduforge--1.3.3--patch-56
    Archive: arch-eduforge@catalyst.net.nz--2004
    Creator: Penny Leach <penny@catalyst.net.nz>
    Date: Wed Sep 15 10:33:23 NZST 2004
    Standard-date: 2004-09-14 22:33:23 GMT
    Modified-files: admin/handlevirus.php
    New-patches: arch-eduforge@catalyst.net.nz--2004/moodle--eduforge--1.3.3--patch-56
    Summary: fix for handlevirus.php since upload class changes
    Keywords: