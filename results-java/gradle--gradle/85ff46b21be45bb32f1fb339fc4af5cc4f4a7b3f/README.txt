commit 85ff46b21be45bb32f1fb339fc4af5cc4f4a7b3f
Author: Adam Murdoch <adam@gradle.com>
Date:   Sun Feb 19 11:41:02 2017 +1100

    Use `FastActionSet` to collect the actions for `Configuration.defaultDependencies()` to improve memory consumption when there are zero or one such actions, which is almost always the case.