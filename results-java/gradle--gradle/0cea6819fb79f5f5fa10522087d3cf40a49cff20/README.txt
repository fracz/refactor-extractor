commit 0cea6819fb79f5f5fa10522087d3cf40a49cff20
Author: Adam Murdoch <adam@gradle.com>
Date:   Sat May 6 06:44:43 2017 +1000

    Some improvements to the formatting of 'no matching variant' and 'too many matching variant' error messages.

    Also replaced some usages of `Factory<String>` with `Describable` to represent the display name of various child objects of `Configuration`.