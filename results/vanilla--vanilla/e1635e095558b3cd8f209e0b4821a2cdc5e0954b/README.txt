commit e1635e095558b3cd8f209e0b4821a2cdc5e0954b
Author: Todd Burry <todd@vanillaforums.com>
Date:   Mon Jul 4 10:17:02 2016 -0400

    Refactor request rewrites into Gdn_Dispatcher->rewriteRequest()

    This is close to a straight extract method refactor in order to make Gdn_Dispatcher->analyzeRequest easier to change.