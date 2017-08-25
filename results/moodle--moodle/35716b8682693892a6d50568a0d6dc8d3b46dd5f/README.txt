commit 35716b8682693892a6d50568a0d6dc8d3b46dd5f
Author: Petr Skoda <skodak@moodle.org>
Date:   Fri Jul 30 20:51:01 2010 +0000

    MDL-22001 filter_text() and filter_string() now use context parameter instead of courseid, PAGE->context is used only as a fallback; moved comment stuff away from format_text() because it does not belong there; filterlib is not using courseid except for legacy filters; fixed coding style in filters;Êimproved php docs; fixed upgrade of filters (should be in plugins, not core)