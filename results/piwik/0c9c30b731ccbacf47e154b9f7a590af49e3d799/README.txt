commit 0c9c30b731ccbacf47e154b9f7a590af49e3d799
Author: Thomas Steur <tsteur@users.noreply.github.com>
Date:   Mon Aug 29 13:30:52 2016 +1200

    Better UI for Piwik 3, more responsive, faster, lots of other fixes  (#10397)

    * improved ui and responsiveness

    * improve rss widget

    * commit changes for ui again, got lost after the last commit

    * fix more tests

    * restoring files

    * fix fonts

    * fix more tests

    * more test fixes

    * fix some system tests

    * fix tests

    * fix system and ui tests

    * fix updater tests

    * make a page as loaded once the callback is called

    * enable verbose

    * more verbose output

    * enable phantomjs debug flag

    * debug should be a phantomjs option

    * trying to fix installation tests

    * fixes #10173 to not compile css files as less

    * trying to minimize js/css requests to hopefully prevent random ui test fails

    * disable verbose mode

    * fix updater and installation

    * lots of bugfixes and ui tweaks

    * fix reset dashboard

    * various bugfixes

    * fix integration tests

    * fix text color

    * hoping to fix installation tests this way

    * cache css/js resources for an hour, should speed up tests and prevent some random issues

    * we need to avoid installing plugins multiple times at the same time when requesting resources

    * finally getting the colors right again

    * fix most tests, more tests for theme

    * use an h2 element for titles for better accessibility

    * fix headline color

    * use actual theme text color (piwik-black)

    * fix small font size was applied on all p elements

    * fix tests

    * now improving all the datatables

    * trying to ignore images for visitor log

    * Revert "trying to ignore images for visitor log"

    This reverts commit ad1ff7267aae14ad905bef130e956c8593c4fb22.

    * fix tests

    * fix we had always ignored a max label width

    * trying to fix file permissions

    * fix more file permissions

    * Improved plugins update API (#10028)

    * refs #7983 let plugins add or remove fields to websites and better settings api

    * * Hide CorePluginsAdmin API methods
    * More documentation
    * Added some more tests

    * improved updates API for plugins

    * better error code as duplicate column cannot really happen when not actually renaming a colum

    Conflicts:
            core/Updates/3.0.0-b1.php
            plugins/CoreUpdater/Commands/Update/CliUpdateObserver.php

    * fix DB field piwik_log_visit.location_provider too small (#10003)

    * fixes #9564 fix DB field piwik_log_visit.location_provider too small

    * use new plugins updater API

    * DB field piwik_log_visit.visit_total_actions too small (#10002)

    * fixes #9565 DB field piwik_log_visit.visit_total_actions too small

    * change type of some db columns that are too small

    * fix tests (#10040)
    Conflicts:
            plugins/CoreAdminHome/Menu.php
            plugins/Goals/Menu.php
            plugins/MobileMessaging/Menu.php
            plugins/SitesManager/Menu.php
            plugins/UsersManager/Menu.php
            tests/PHPUnit/System/expected/test_apiGetReportMetadata__API.getWidgetMetadata.xml

    * fix more file permissions

    * repair more file permissions

    * repair more file permissions

    * trying to make ui tests work again, the table was missing

    * fix some encoding issues

    * cross browser fixes and usability improvement

    * move back the config icon, need to find a better solution later

    * more cross browser fixes

    * bugfixes

    * fix ui tests

    * fix encoding issue

    * fix various issues with the ui tests when a test gets aborted

    * also skip this visitor log test when aborted

    * there were 3 css files that were loaded separately, merge them instead into one css

    * forgot to add the actual manifest

    * do not add manifest if custom logo is specified

    * load font css files first as it was before merging them into big css

    * fix link icon was not aligned anymore

    * minor fixes

    * setting it back to 4px

    * in popovers the font variable was always ignored and a different font loaded

    * forgot to update screenshots

    * fix remaining tests

    * this should fix an update error

    * added 3 new widgets system check, system summary and plugin updates

    * tweak new widgets content

    * no page reload when changing date or segment

    * in admin home show only enabled widgets

    * refs #10295 use getMockBuilder instead of deprecated getMock

    * fix some ui tests

    * fix various bugs

    * fix more tests

    * fix ui tests

    * add a space between loading image and loading message

    * fix docs so they appear on developer.piwik.org

    * improved documentation

    * introduce new Widget::renderTemplate method for consistency with controllers

    * remove no longer needed files

    * testing system fonts

    * fix strong was not really bold

    * more useful system summary

    * remove ubuntu font

    * fix most tests and removed most em elements

    * fix tests

    * fix headline was very thin

    * update submodule

    * update submodules

    * update submodule

    * fix failing ui tests

    * update submodules