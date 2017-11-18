commit aa9d7298c1a7709109e7180cc545fd0d3c1c3f80
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Wed Mar 5 16:51:51 2014 +0400

    refactoring branch actions

    *common branch actions moved to a separate class;
    *bookmark actions extends common branch actions to avoid code duplication;
    *common child  branch abstract action created as a base class for every other branch and bookmarks actions