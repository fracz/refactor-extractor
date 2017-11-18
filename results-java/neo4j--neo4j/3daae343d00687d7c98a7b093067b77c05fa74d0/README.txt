commit 3daae343d00687d7c98a7b093067b77c05fa74d0
Author: Anton Klaren <anton.klaren@neotechnology.com>
Date:   Tue Aug 8 13:30:12 2017 +0200

    Deprecated Setting.from(Configuration) method in favor or Configuration.get(Setting).
    Added convenient methods for Config.augment() and Config.defaults() and refactor code to use those.
    Refactored ConfigurationValidator to actually use the Config instead of a Map<String,String>.