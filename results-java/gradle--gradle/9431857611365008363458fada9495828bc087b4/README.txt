commit 9431857611365008363458fada9495828bc087b4
Author: Adam Murdoch <adam@gradle.com>
Date:   Fri Dec 11 11:15:40 2015 +1100

    More improvements to validation of a RuleSource type.

    - Fixed NPE when a rule method has no parameters.
    - Collect more kinds of validation failures, rather than failing on first of these kinds of failures.
    - Validate that the first parameter of a `@Rules` metod is assignable to `RuleSource`

    +review REVIEW-5739