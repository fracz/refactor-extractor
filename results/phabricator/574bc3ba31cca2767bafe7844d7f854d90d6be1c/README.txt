commit 574bc3ba31cca2767bafe7844d7f854d90d6be1c
Author: indiefan <indiefan@gmail.com>
Date:   Tue Feb 5 18:45:20 2013 -0800

    First pass at decoupling Phabricator bot behavior from the protocol it's running on, this pulls the connection, reading, and writing functionalities out of the bot itself and into the adapter.

    Summary:
    Ugh, just wrote out a huge message, only to lose it with a fat-fingered ctrl-c. Le sigh.

    First pass at decoupling the bot from the protocol. Noticeably absent is the command/message coupling. After this design pass I'll give that a go. Could use some advice, thinking that handlers should only create messages (which can be public or private) and not open ended, undefined 'commands'. The problem being that there needs to be some consistant api if we want handlers to be protocol agnostic. Perhaps that's a pipedream, what are your thoughts?

    Secondly, a few notes, design review requests on the changes i did make:
     # Config. For now i'm passing config through to the adapter. This was mainly to remain backwards compatible on the config. I was thinking it should probably be namespaced into it's own subobject though to distinguish the adapter config from the bot config.
     # Adapter selection. This flavor is the one-bot-daemon, config specified protocol version. The upside is that in the future they won't have to run different daemons for this stuff, just have different config, and the door is open for multiple protocol adapters down the road if need be. The downside is that I had to rename the daemon (non-backwards compatible change) and there will need to be some sort of runtime evaluation for instatiation of the adapter. For now I just have a crude switch, but I was thinking of just taking the string they supply as the class name (ala `try { new $clasName(); } catch...`) so as to allow for homegrown adapters, but I wasn't sure how such runtime magic would go over. Also, an alternative would be to make the PhabricatorBot class a non-abstract non-final base class and have the adapters be accompanied by a bot class that just defines their adapter as a property. The upside of which is backwards compatibility (welcome back PhabricatorIRCBot) and perhaps a little bit clearer plugin path for homegrowners.
     # Logging. You'll notice I commented out two very important logging lines in the irc adapter. This isn't intended to remain commented out, but I'm not sure what the best way is to get logging at this layer. I'm wary of just composing the daemon back down into the adapter (bi-directional object composition makes my skin crawl), but something needs to happen, obviously. Advice?

    That's it. After the feedback on the above, you can either merge down, or wait until i finish the command/message refactor if you don't think the diff will grow too large. Up to you, this all functions as is.

    Test Plan: Ran an irc bot, connected, read input, and wrote output including handler integration.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Maniphest Tasks: T2462

    Differential Revision: https://secure.phabricator.com/D4757