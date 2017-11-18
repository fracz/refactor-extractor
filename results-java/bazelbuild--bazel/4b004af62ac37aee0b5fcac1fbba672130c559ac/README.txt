commit 4b004af62ac37aee0b5fcac1fbba672130c559ac
Author: Ulf Adams <ulfjack@google.com>
Date:   Wed Sep 16 10:22:26 2015 +0000

    Change checkRuntime to take a CommandEnvironment instead.

    I wanted to merge it into beforeCommand, but the reporter isn't set up yet
    when we call beforeCommand. Hopefully we can refactor the code to merge it
    in the future.

    --
    MOS_MIGRATED_REVID=103179268