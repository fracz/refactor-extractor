commit a5fe59128011034dd0f3a6f3721bd4aec61bc455
Author: David Mudrak <david@moodle.com>
Date:   Wed May 11 03:28:39 2011 +0200

    MDL-27376 MDL-27377 MDL-27378 Backup converters refactoring - still work in progress

    This patch mainly adds support for dispatching path-start and path-end events,
    defines API to access backup_ids_temp table. Some ideas emailed by Mark
    after his first review of the code are incorporated already.