commit bd5217287e84d8a2fdc457dd07ce63d78c5835b4
Author: Matthieu Aubry <mattab@users.noreply.github.com>
Date:   Thu Sep 29 15:03:02 2016 +1300

    2.16.3-rc1 (#10590)

    * Fix depraction test: use assertDeprecatedMethodIsRemovedInPiwik3

    * Fix Scheduled Reports sent one hour late in daylight saving timezones (#10443)

    * convert hour to send report to/from UTC, to ensure it isn't affected by daylight savings

    * adds update script to change existing scheduled reports to use utc time

    * code improvement

    * adds missing param

    * Added new event Archiving.makeNewArchiverObject to  allow customising plugin archiving  (#10366)

    * added hook to alllow plugin archiving prevention

    * cr code style notes

    * reworked PR to fit CR suggestions

    * added PHPDoc for hook

    * Event description more consistent

    * UI tests: minor changes

    * Comment out Visitor Log UI tests refs #10536

    * Adds test checking if all screenshots are stored in lfs

    * removed screenshots not stored in lfs

    * readds screenshots to lfs

    * 2.16.3-b4

    * Issue translation updates against 2.x-dev

    * language update

    * Fix bug in widget list remove where the JSON object becomes array

    * 2.16.3-rc1

    * console command custom-piwik-js:update should work when directory is writable and file does not exist yet (#10576)

    * followup #10449

    * Fix test
    (cherry picked from commit fac3d63)

    * Prevent chmod(): No such file or directory

    * Automatically update all marketplace plugins when updating Piwik (#10527)

    * update plugins and piwik at the same time

    * make sure plugins are updated with piwik

    * use only one try/catch

    * reload plugin information once it has been installed

    * make sure to clear caches after an update

    * fix ui tests

    * make sure to use correct php version without any extras

    * Additional informations passed in the hook "isExcludedVisit" (issue #10415) (#10564)

    * Additional informations passed in the hook "isExcludedVisit" (issue #10415)

    * Added better description to the new parameters

    * Update VisitExcluded.php

    * Remove two parameters not needed as better to use the Request object

    * Update VisitExcluded.php

    * remove extra two parameters in VisitExcluded constructor to prevent confusion (#10593)

    * Update our libs to latest https://github.com/piwik/piwik/issues/10526

    * Update composer libraries to latest https://github.com/piwik/piwik/issues/10526

    * Update log analytics to latest

    * When updating the config file failed (or when any other file is not writable...), the Updater (for core or plugins) will now automatically throw an error and cancel the update (#10423)

    * When updating the config file failed (or when any other file is not writable...), the Updater (for core or plugins) will now automatically throw an error and cancel the update

    * add integration test to check the correct exception is thrown when config not writabel

    * New integration test for updater

    * Make test better

    * When opening the visitor profile, do not apply the segment (#10533)

    * When opening the visitor profile, do not apply the segment

    * added ui test for profile but does work for me

    * next try to make ui test work

    * add expected screenshot

    * added missing doc