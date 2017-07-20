commit 431e2bee6e7c827bf843d728712798b8d81852cf
Author: indiefan <indiefan@gmail.com>
Date:   Thu Feb 7 06:31:29 2013 -0800

    First (rough) pass at campfire protocol adapter for bot.

    Summary:
    Decided the best approach for refactoring the message/command stuff would be to actually start implementing the campfire adapter to get a better idea of what the abstractions should look like. It feels awkward and unwieldy trying to maintain the irc command interface (notice the message instantiation in the `processReadBuffer()` method. However, i'm still not clear what the best approach is without requiring a re-write of nearly all the existing handlers and defining essentially a custom dsl on top of irc's.

    I suppose given that alternative, implementing to irc's dsl doesn't sound all that bad. Just feels like poor coupling.

    Also, I know that there is some http stuff in libphutil's futures library, but the https future is shit and I need to do some custom curlopt stuff I wasn't sure how to do with that. But if you think this should be refactored, let me know.

    I tested this with the ObjectHandler (messages with DXXX initiate the bot to respond with the title/link just as with irc), but beyond that, I haven't tried any of the other handlers, so if there are complications you think i'm going to run into, just let me know (this is one of the reasons for requesting review early on).

    Also, this diff is against my last one, even though that hasn't been merged down yet. It was starting to get large and I'd prefer to keep to two conversations separate.

    Fixing some lint issues.

    Test Plan: Ran the bot with the Object Handler in campfire and observed it behaving properly.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Maniphest Tasks: T2462

    Differential Revision: https://secure.phabricator.com/D4830