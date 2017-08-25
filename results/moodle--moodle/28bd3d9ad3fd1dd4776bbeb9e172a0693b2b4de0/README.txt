commit 28bd3d9ad3fd1dd4776bbeb9e172a0693b2b4de0
Author: Petr Skoda <skodak@moodle.org>
Date:   Tue Aug 17 12:33:30 2010 +0000

    MDL-23824 CLI script improvements - just define('CLI_SCRIPT', true) before require config.php; all incorrect uses of cli and web scripts are detected; refactored cron script - now in two separate sctipts; fix cli inline docs and help - we have to sudo to apache account; standardised cli script locations in auth plugins