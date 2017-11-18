commit a70ad41b7f4085e853d85effd61e11a972dfbd37
Author: Ben Butler-Cole <ben@bridesmere.com>
Date:   Thu Feb 25 16:58:01 2016 +0000

    Simplify Config settings update logic

    As well as refactoring, this removes inconsistently implemented
    filtering of null config values. That was originally there because
    internally we use a Map which doesn't allow null values. But there is no
    reason for null values to be supplied, so it's better to just fail
    loudly if someone does this rather than silently ignoring them.