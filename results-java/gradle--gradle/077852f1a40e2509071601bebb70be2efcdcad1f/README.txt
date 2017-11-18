commit 077852f1a40e2509071601bebb70be2efcdcad1f
Author: Adam Murdoch <adam@gradle.com>
Date:   Mon Mar 28 17:08:53 2016 +1100

    Fixed single use worker processes so that it actually works.

    Previously would hang when used from a plugin. Also improved handling to get rid of the hang when the worker process crashes for some reason.