commit 86f6eec3276204491f4336fb5526544e7f0fd11d
Author: Petr Skoda <skodak@moodle.org>
Date:   Thu Sep 2 18:49:31 2010 +0000

    MDL-23184 PARAM_CLEANHTML is work with real html markup only, it does not do our custom FORMAT_MOODLE tweaks anymore, luckily this was not supposed to be used before storage to database in 1.9, that means there should not be any BC issues;Êimproved docs