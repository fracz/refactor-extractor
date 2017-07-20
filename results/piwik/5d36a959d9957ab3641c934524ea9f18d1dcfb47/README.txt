commit 5d36a959d9957ab3641c934524ea9f18d1dcfb47
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Mon Aug 25 09:24:44 2014 +0200

    refs #5863 we need to load core translations upfront as metadataLoader will try to load plugin translation directly after loading the plugin when the plugin translations are not loaded yet. Maybe better fix would be to defer loading the plugin getInformation until needed. Could also bring performance improvement