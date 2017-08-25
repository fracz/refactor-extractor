commit ed996ede966005745b56d90f887d3f9e7df9c822
Author: mudrd8mz <mudrd8mz>
Date:   Mon Aug 31 15:23:02 2009 +0000

    MDL-20191 Subplugins are uninstalled together with the parent plugin

    This is a first working implementation I have. I need to so I can
    continue with the Workshop development. I think the performance can be
    significantly improved here as the location of the subplugins is
    evaluated atr least twice, I guess.