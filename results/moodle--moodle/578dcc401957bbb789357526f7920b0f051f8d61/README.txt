commit 578dcc401957bbb789357526f7920b0f051f8d61
Author: skodak <skodak>
Date:   Mon Nov 14 22:53:44 2005 +0000

    add GLOBALS overwrite protection and improved magic_quotes_gpc hack SC#191, SC#184, SC#92; merged together with Jon's last patch from MOODLE_15_STABLE
    also removed the broken unregister_globals() function