commit cb9f090397c7bb5090e72ee0a3386badadaa4bfa
Author: Michael Babker <mbabker@flbab.com>
Date:   Sun Sep 18 10:40:25 2016 -0500

    Session interaction refactoring (#10905)

    * Allow lazy starting a session

    * Defer starting sessions in the installer

    * Defer starting session in JApplicationCms

    * Deprecate creating sessions in JFactory

    * Start restructuring session load sequence in JApplicationCms

    * Add JSession object to onAfterSessionStart event trigger

    * Override afterSessionStart for install app to not store the user

    * Expand the database storage object for better API

    * Fix session listener declaration

    * Restructure session bootup and when the session metadata operations run

    * 3.5 -> 3.6

    * Remove option, out of scope

    * Tweak things so the behavior is closer to current API

    * Undefined variable

    * Get store name from session object

    * Remove this, out of scope

    * PHPCS fix

    * Deep rooted application dependencies are my absolute favorite

    * Move comment, doc blocks, consistent method signature

    * Again on the consistency, we don't like E_STRICT errors

    * Nevermind, confused myself with checkSession, carry on...

    * Until the day the unit tests no longer follow the doc block code styles...

    * Lost line in merge

    * One more merge conflict fix

    * This internal coupling is addressed

    * Fix query from merge