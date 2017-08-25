commit cba94cec14417888295213957dec9f29e3713ac6
Author: Petr Škoda <commits@skodak.org>
Date:   Fri Jul 12 22:34:12 2013 +0200

    MDL-40563 improve yui serving performance

    It is not necessary to do full page init to access the plugin locations API.