commit 9ad8b423c2fbf6c758efd9c1989379a44a59dc52
Author: Petr Škoda <commits@skodak.org>
Date:   Fri Jul 12 22:38:56 2013 +0200

    MDL-40563 improve jquery serving performance

    It is not necessary to do full page init to access the plugin locations API.