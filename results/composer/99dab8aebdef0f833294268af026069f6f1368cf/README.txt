commit 99dab8aebdef0f833294268af026069f6f1368cf
Author: Kunal Mehta <legoktm@gmail.com>
Date:   Sat May 30 23:24:20 2015 -0700

    Move VersionParser::formatVersion() to BasePackage::getFullPrettyVersion()

    Working towards #3545.

    formatVersion() does not belong in VersionParser since it depends upon a
    Package object, and is creating a more complete pretty formatted
    version, not parsing anything.

    The new getFullPrettyVersion() method can be seen as an extension to
    getPrettyVersion(), and is located in BasePackage as a result.

    Callers to VersionParser::formatVersion() were not updated in this
    commit to demonstrate that no functionality was changed in this
    refactor. They will be updated in a follow up commit.