commit a9a9bdba55422730a0c5ac52f7f1e88b98e348ee
Author: martinlanghoff <martinlanghoff>
Date:   Fri Apr 1 05:55:10 2005 +0000

    Merged from MOODLE_14_STABLE - insert_record() - major efficiency improvements for Postgres databases on insert. Also fixed many calls to insert_record() which discard the returned record id to not ask for the record id.