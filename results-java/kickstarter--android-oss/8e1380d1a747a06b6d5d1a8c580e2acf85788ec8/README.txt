commit 8e1380d1a747a06b6d5d1a8c580e2acf85788ec8
Author: Brandon Williams <mbw234@gmail.com>
Date:   Mon Apr 18 07:10:37 2016 -0400

    Android Pay

    * Update dependencies so that we can include play-services 8.4

    * Stubbing some Android Pay views.

    * Intercept android pay requests, wip

    * Beginning work on a confirmation activity.

    * Begin wiring up results of authorized android pay payments

    * Bringing some android pay stuff into checkout.

    * Extract stripe token data from wallet

    * Handle alternative android pay uri form

    * Some work on confirmations screen.

    * Close to getting a full wallet request happening.

    * Can now load a full wallet from Android Pay.

    * Update circle.yml for new build tools.

    * Build tools back down to 23.0.1

    Don't think we needed to bump, and it was causing circle issues

    * Don't check for play services if in tests

    This is horrible, check the comment. I'm sorry.

    * I think everything is in place now with android pay.

    * Apparently activity request codes cant be too large.

    * Cleaned up some show/hide logic.

    * Performing some cleanup.

    * Performing some cleanup.

    * Dont need a style since it's not displaying.

    * Removed old debug toasting.

    * Simplified AndroidPayAuthorizationPayload to not be nested.

    * fixing more merge conflicts.

    * Add android pay capability to environment.

    * Fixed some checkstyle rules.

    * Made PlayServicesCapability singleton and don't invoke google api client if no play services.

    * Remove dupe activity in manifest.

    * Switch conditional check.

    * Unstub some data.

    * Removed unused imports.

    * Small refactor.

    * Fixing up more stuff.

    * Revert later.

    * Address some PR feedback

    * Favor using BindString

    * Move some code out to utils

    Trying to keep the VM pretty lean

    * Add missing bindToLifecycle calls

    * Remove unnecessary params

    * Fix duplicated strings

    I think these had been manually inserted, so they weren't caught
    when we merged in the strings that had been fetched from the API

    * Fix parsing of stripe token

    * Remove unused import

    * Fixed bug will over loading webviews during android pay flow.

    * Revert "Revert later."

    This reverts commit dc7f09ff784abb8cee5b7937be660276d688e249.

    * Fixed a bunch of lint warnings.

    * Small fixes and basic koala tracking.

    * Ignore Jsoup TrustAllX509TrustManager warning

    * Added error messaging.

    * Clean up.

    * Rename variable.

    * Fixed isFalse.

    * Print error code for android pay.

    * Fix error code.

    * -1 means NO ERROR.

    * Use build variant instead of string to determine android pay environment.

    * Pattern matches an optional 'pledge/' string

    * Rename method, this no longer uses toast

    * Ignore typography dashes lint error