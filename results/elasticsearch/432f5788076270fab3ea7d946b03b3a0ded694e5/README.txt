commit 432f5788076270fab3ea7d946b03b3a0ded694e5
Author: Igor Motov <igor@motovs.org>
Date:   Wed Feb 18 10:21:17 2015 -0500

    Internal: refactor settings filtering

    Refactor how settings filters are handled. Instead of specifying settings filters as filtering class, settings filters are now specified as list of settings that needs to be filtered out. Regex syntax is supported. This is breaking change and will require small change in plugins that are using settingsFilters. This change is needed in order to simplify cluster state diff implementation.

    Contributes to #6295