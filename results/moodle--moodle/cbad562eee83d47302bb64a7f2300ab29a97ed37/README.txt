commit cbad562eee83d47302bb64a7f2300ab29a97ed37
Author: Petr Skoda <skodak@moodle.org>
Date:   Fri Nov 19 03:40:43 2010 +0000

    MDL-25314 improved prevention of output buffering + detection of misconfigured servers
    Scripts that do not want buffered output just define NO_OUTPUT_BUFFERING before including config.php.
    The fileserving code now checks if the headers are already sent which detects misconfigured servers.