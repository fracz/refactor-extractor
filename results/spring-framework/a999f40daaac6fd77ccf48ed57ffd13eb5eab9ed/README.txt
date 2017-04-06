commit a999f40daaac6fd77ccf48ed57ffd13eb5eab9ed
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue Mar 21 17:32:14 2017 -0400

    Polish + minor refactoring of SSE reader and writer

    Instead of accepting List<Encoder|Decoder> and then look for the first
    to support JSON, always expect a single JSON [Encoder|Decoder] and use
    that unconditionally.

    When writing use the nested ResolvableType instead of the Class of the
    actual value which should better support generics.

    Remove the SSE hint and pass "text/event-stream" as the media type
    instead to serve as a hint. We are expecting a JSON encoder and using
    it unconditionally in any case so this should be good enough.