commit a247efdd5c1999b4351e03f1cc6d724f86e7260c
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Aug 17 15:18:19 2015 +0300

    Reparse docstring and use parameter type substring for live template placeholder

    Also I removed redundant methods from Substring and slightly improved
    regexes used for parsing EpyDoc and Shpinx docstrings.