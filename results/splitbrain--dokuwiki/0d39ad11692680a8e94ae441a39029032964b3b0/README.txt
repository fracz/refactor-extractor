commit 0d39ad11692680a8e94ae441a39029032964b3b0
Author: Anika Henke <anika@selfthinker.org>
Date:   Sat Dec 7 20:41:17 2013 +0000

    fixed editing of RTL and LTR scripts mix (including a lot of wiki syntax) being a mess

    Bi-directionality is generally a huge pain in the arm, browser vendors
    only added a few improvements quite recently. This fix only works in
    Mozilla and Webkit currently. Affected syntax was links and media and all
    opening and closing syntax (e.g. code and del and various plugin syntax).