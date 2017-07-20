commit 587cc39e0362719332d410b7a4d5ddcc68788eeb
Author: Thomas Steur <tsteur@users.noreply.github.com>
Date:   Tue Nov 15 14:03:59 2016 +1300

    Update Marketplace to work with new API (#10799)

    * starting to port marketplace to piwik 3

    * updating tests

    * fix translation key

    * fix various issues

    * use material select

    * fix plugin upload

    * deprecate license_homepage plugin metadata and link to a LICENSE[.md|.txt] file if found (#10756)

    * deprecate license_homepage plugin metadata, and link to a LICENSE[.md|.txt] file if found

    * Make license view HTML only without menu

    * fix tests and update

    * fix some links did not work

    * we need to show warnings even when plugin is installed, not only when activated. otherwise it is not clear why something is not downloadable

    * fix install was not working

    * improved responsiveness of marketplace

    * fix more tests

    * fix search was shown when only a few plugins are there

    * fix ui tests

    * fix some translations

    * fix tests and remove duplicated test