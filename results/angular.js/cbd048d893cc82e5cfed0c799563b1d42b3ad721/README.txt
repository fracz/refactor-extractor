commit cbd048d893cc82e5cfed0c799563b1d42b3ad721
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Tue Mar 15 17:42:39 2016 +0100

    fix(ngMessages): don't crash when nested messages are removed

    Under specific circumstances, ngMessages would go into an infinite loop and crash the
    browser / page:
    - At least two ngMessage elements are wrapped inside another element (e.g. ngTransclude)
    - The first message is currently visible
    - The first message is removed (e.g. when the whole ngMessages element is removed by an ngIf)

    When a message is removed, it looks for a previous message - in this specific case it would misidentify
    the second message for a previous message, which would then cause the first message to be marked as the
    second message's next message, resulting in an infinite loop, and crash.

    This fix ensures that when searching for previous messages, ngMessage walks the DOM in a way so
    that messages that come after the current message are never identified as previous messages.

    This commit also detaches and destroys all child ngMessage elements when the ngMessages element is
    destroyed, which should improve performance slightly.

    Fixes #14183
    Closes #14242