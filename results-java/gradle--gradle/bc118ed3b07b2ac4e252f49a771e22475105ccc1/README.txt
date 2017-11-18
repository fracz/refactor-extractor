commit bc118ed3b07b2ac4e252f49a771e22475105ccc1
Author: Adam Murdoch <adam@gradle.com>
Date:   Sun Feb 19 12:53:23 2017 +1100

    Changed `DefaultConfiguration` to defer creation of its `ResolutionStrategy` until it is required. This object is quite heavy weight, so don't create it until it needs to be configured or the configuration resolved. This could be further improved later.