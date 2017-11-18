commit ed76bae41e04644e115e3c6a3920d7bd605b7535
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Mar 30 12:47:52 2016 +0200

    Allow RawMessage's payload to be empty (but not null)

    RawMessage previously threw an exception when the payload was empty during construction. This lead to errors in the call sites, which, depending on the input, could cause connections and/or threads to be closed, e.g. the Kafka consumer would be shut down.

    With this change we accept empty payloads, but discard them before inserting them into the input buffer. Since this happens in a central place we should cover all available inputs.
    We also track the number of discarded empty messages and show this with each input next to the throughput metrics. Empty payloads usually indicate a sender error but can be safely ignored for now. Discarded messages do not count towards the local or global incoming messages as they are discarded before being journaled and processed by Graylog.

     This change also improved the exception logging in the NettyTransport, where we now are way less verbose when faced with a "Connection reset by peer". We don't currently write significant information back to the client so we don't need to log the entire (meaningless) stacktrace here. The information is still available on TRACE.

    fix #1584