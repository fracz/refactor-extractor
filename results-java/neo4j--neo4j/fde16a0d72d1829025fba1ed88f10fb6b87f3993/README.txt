commit fde16a0d72d1829025fba1ed88f10fb6b87f3993
Author: Anton Klaren <anton.klaren@neotechnology.com>
Date:   Tue Aug 15 09:31:07 2017 +0200

    Removed hard coded config filenames in favor of constant in Config.
    Renamed setting to buildSetting when additional parameters are needed to make it clear that a builder is returned.
    Refactored identifiersFromPrefix to to use the original annotation Group, that got lost in previous refactoring.