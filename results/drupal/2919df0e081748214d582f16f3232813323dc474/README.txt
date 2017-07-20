commit 2919df0e081748214d582f16f3232813323dc474
Author: Dries Buytaert <dries@buytaert.net>
Date:   Tue Mar 15 21:07:49 2005 +0000

    - More improvements by Morbus, Goba, TDobes et al:

    Per TDobes' comments:

    * INSTALL.txt corrected to use 4.3.3, not 4.33.
    * .htaccess: removed allow_call_time_pass_reference. two confirmations that a) the setting was wrong in the first place, b) there were no adverse affects for the incorrect setting, c) the PHP docs say it is deprecated.
    * .htaccess: removed short_open_tag. Running grep -r "<? " * across the entire directory tree of both core and contributions only brought up contributions and no core. I agree that the fuller form is better. The following contributions will need to be updated: modules/edit_template/edit_template.module, sandbox/garym/themes/marvin_2k/templates/page.tpl.php, sandbox/killes/compare.php, sandbox/paolino/import/click.php, themes/spreadfirefox/block.tpl.php. For error's sake, I also did a manual verification for "<?" (no space) across core and came up against nothing in addition to the above contribs.

    Per Goba's comments:

    * .htaccess: Moved session.auto_start back.
    * sites/default/settings.php: Removed track_vars.

    Per mailing list comments:

    * INSTALL.txt: Added text about the files/ directory, creating it, and permissions.
    * INSTALL.txt: Added an example of why Drupal requires cron (the search.module) in an attempt to justify why a crontab is a good, nay, required step.

    And my own further analities:

    * .htaccess: cleaned up some whitespace valleys (i hate 'em, hate 'em!) and fixed a few stray colons.
    * settings.php: minor whitespace error.