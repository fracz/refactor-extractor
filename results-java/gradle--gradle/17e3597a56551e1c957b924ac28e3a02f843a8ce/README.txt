commit 17e3597a56551e1c957b924ac28e3a02f843a8ce
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Sep 10 14:32:18 2012 +0200

    Unignored the test, fixed the issue with empty resolution result not being provided for the configuration.

    I didn't want to create InternalResolvedConfiguration nor InternalLenientConfiguration so I decided to hook up to the new method on the ConfigurationInternal type. This seems to be a lesser evil until we get some serious refactoring/deprecation around resolved/lenient configuration.