commit a43ec9ebac16122b1709e201528d36a9c4ec3c7a
Author: Jun Pataleta <jun@moodle.com>
Date:   Fri Jul 21 10:59:43 2017 +0800

    MDL-59538 core_calendar: PR fixes

    * Fix eslint warnings about promises.
    * Simplify event handling.
    * Prevent double assignment of calendar subscription property for in
      core_calendar_external::get_calendar_event_by_id() for readability.

    Part of MDL-59333.