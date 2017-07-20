commit aeb3a12e13aedb93711fad2081895b31c5ce836e
Author: Bob Trahan <btrahan@phacility.com>
Date:   Wed Nov 12 11:31:54 2014 -0800

    Config - improve lock message for option `phabricator.uninstalled-applications`

    Summary: Fixes T6175. This particular option is only editable via the Applications application so let the user know that.

    Test Plan: saw error message and clicked through to applications app. tried another locked option and saw old message

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T6175

    Differential Revision: https://secure.phabricator.com/D10841