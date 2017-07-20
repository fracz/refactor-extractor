commit c1a88d5d6733f1cd17fe677456cb445edd6a7afd
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat Dec 16 08:39:01 2000 +0000

    Again, a large batch of updates - I'm twisting things around here:

     1. improved .htaccess to be more "secure": to keep prying
        eyes out

     2. rewrote the administration section from scratch using a
        modular approach

     3. improved the information gathered by error.php - we can
        now (hopefully) track what bots are crawling us.

     4. fixed a bug in submit.php, fixed a bug in theme zaphod,
        fixed a bug in theme marvin.

     5. rewrote cron from scratch - it now interfaces with
        modules as it should have been from the beginning.
        Very cool if you ask me - it can use UNIX/Linux
        crontabs.

     6. updated widget.inc to be module aware - needs more
        work though - maybe this afternoon?

     7. updated most modules: small bugfixes, improvements, and
        even the documentation

     8. removed diary.php and made it a module - you can now
        run a drop.org site without a diary system if someone
        would prefer so

     9. updated all themes to use the new modules where
        appropriate

     10. added a robots.txt because the error message in the
         watchdog become annoying.

     11. added the new configuration system (mutliple vhosts
         on the same source tree) - use hostname.conf instead
         of config.inc

     12. removed calendar.inc and made it a module

     13. added format_interval() to functions.inc (UnConeD)

     14. whatever I forgot ...