commit 355d25e73d90f3174db459a5a380193e0505ada4
Author: Dries Buytaert <dries@buytaert.net>
Date:   Wed Jun 4 18:24:39 2003 +0000

    - Bugfix: renamed the SQL field 'types' to 'nodes' because 'types' is a reserved keyword in MySQL 4.  This fixes critical bug #1618.  Patch by Marco.

      ==> This fix requires to run update.php!

    - Bugfix: made sessions work without warnings when register_globals is turned off. The solution is to use $_SESSION instead of session_register().  This fixes critical bug #1797.  Patch by Marco.

    - Bugfix: sometimes error messages where being discarded when previewing a node.  Patch by Craig Courtney.

    - Bugfix: fixed charset problems.  This fixes critical bug #1549.  Patch '0023.charset.patch' by Al.

    - Code improvements: removed some dead code from the comment module.  Patch by Marco.

    - Documentation improvements: polished the node module help texts and form descriptions.  Patch '0019.node.module.help.patch' by Al.

    - CSS improvements all over the map!  Patch '0021.more.css.patch' by Al.

    - GUI improvements: improved the position of Druplicon in the admin menu.  Patch '0020.admin.logo.patch' by Al.

    - GUI improvements: new logos for theme Marvin and theme UnConeD.  Logos by Kristjan Jansen.

    - GUI improvements: small changes to the output emitted by the profile module.  Suggestions by Steven Wittens.

    - GUI improvements: small fixes to Xtemplate.  Patch '0022.xtemplate.css.patch' by Al.

    TODO:

    - Some modules such as the buddy list module and the annotation module in the contributions repository are also using session_register().  They should be updated.  We should setup a task on Drupal.

    - There is code emitting '<div align="right">' which doesn't validate.

    - Does our XML feeds validate with the charset changes?

    - The forum module's SQL doesn't work properly on PostgreSQL.