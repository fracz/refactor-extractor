commit 77cb4d2cd02d23464fdfd61745b98030d17faa9f
Author: Selim Cinek <cinek@google.com>
Date:   Fri Jun 2 13:43:41 2017 -0700

    Fixed an issue where would could be left with empty notifications

    If the state between systemui and the notification manager got
    out of sync, we would not immediately remove the notifications when
    using clear all.

    This could lead to holes in the notification shade
    temporarily. While this isn't a fix, it helps avoiding these ugly
    states, similarly to how we already do it when manually swiping.

    This also improves that only notifications that are visible are
    actually participating in the clear all animations instead of
    all of them.

    Test: manual, hit clear all observe animations / normal clearing
    Bug: 62171447
    Change-Id: I83d0f3cb0bae4bc43bf35d3f9399cebc05259680