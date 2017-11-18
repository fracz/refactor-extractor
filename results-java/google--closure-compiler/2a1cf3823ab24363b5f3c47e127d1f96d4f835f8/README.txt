commit 2a1cf3823ab24363b5f3c47e127d1f96d4f835f8
Author: johnlenz <johnlenz@google.com>
Date:   Wed Nov 30 12:30:11 2016 -0800

    Two changes to improve the performance of the conformance rules.

    The first, the bulk of the time in BanUnknownTypedClassPropsReferences is spent checking if the filename matches the whitelisted files.  Reverse the order of checks so that the filename checks only occur after a violation might have been reported.

    The second, about 25-35% percent of the time in BannedName is spent splitting and building substrings, remove this time by building a Node tree to compare against ahead of time and use that.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=140637035