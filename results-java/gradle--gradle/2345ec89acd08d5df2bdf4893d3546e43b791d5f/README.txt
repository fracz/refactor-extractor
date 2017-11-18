commit 2345ec89acd08d5df2bdf4893d3546e43b791d5f
Author: Luke Daley <ld@ldaley.com>
Date:   Wed Jul 15 16:04:07 2015 +1000

    Actually test the TestKit samples.

    Also:

    - Added some better (still needs improvement) handling of not being able to find a distribution to back the runner
    - Removed wrapping of failures that aren't build failures (e.g. daemon disappeared)

    +review REVIEW-5533