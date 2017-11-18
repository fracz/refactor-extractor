commit b6f15a17e0f0a260f6e3a81040d227142da4e191
Author: Oliver Woodman <olly@google.com>
Date:   Tue Sep 15 13:44:52 2015 +0100

    TTML improvements.

    - do not denormalize styles at parsing time but only put normalized style info
    into TtmlNode tree. Resolve styles on demand when Cues are requested for a
    given timeUs.
    - create TtmlRenderUtil to have static render functions separate
    - added unit test for TtmlRenderUtil
    - adjusted testing strategy for unit test to check resolved style on Spannables after rendering