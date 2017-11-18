commit 2f0bc0be1bdfc424042d0ecc48f74896d17c5d31
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Feb 20 19:21:39 2012 +0100

    Stopped using the getInputArguments() for the background daemon to avoid some problems. We might still use getInputArguments() in future but then we need to use selective values rather than using entire content. Details:

    -Background daemon is now started with information about it's startup jvmopts. It's simplest implementation: all jvm opts are included as-is at the end of the parameters' list. This way we avoid some complexities around encoding/serializing the list into a single argument. OTOH, the startup arg list will look quite awkward.
    -Foreground daemon cannot be really told what's his context. So it is using the inputArguments, however I decided to only use the arguments we currently considered as 'managed' (see the JvmOptions class). I think it's a sufficient approximation at this point. The foreground daemon respects GRADLE_OPTS but background doesn't. This leads to troubles in matching the right daemon. Also, I'm not sure what's the decision on the respecting the 'gradle.properties' by the foreground daemon - should it respect it and effectively become sort of a 'project-specific' foreground daemon? Pretty soon we need figure out what's the future of the foreground daemon because now and then in introduces complexity to our implementation and narrows down some implementation choices.
    -Some tweaks/improvements of coverage in tests.