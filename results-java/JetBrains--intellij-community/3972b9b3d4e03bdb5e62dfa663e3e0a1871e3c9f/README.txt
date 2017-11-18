commit 3972b9b3d4e03bdb5e62dfa663e3e0a1871e3c9f
Author: Yaroslav Lepenkin <yaroslav.lepenkin@jetbrains.com>
Date:   Thu Jun 11 17:12:40 2015 +0300

    [Formatter] Provided ability to skip building injected blocks by setting AbstractBlock "myBuildInjectedBlocks"  flag.
    [JavaFormatter] If this flag is set no "InjectedLanguageUtil.hasInjections" check will be performed, which dramatically improves performance of formatter-based indent detector.