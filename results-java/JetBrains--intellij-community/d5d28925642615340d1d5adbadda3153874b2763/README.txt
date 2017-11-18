commit d5d28925642615340d1d5adbadda3153874b2763
Author: Roman Shevchenko <roman.shevchenko@jetbrains.com>
Date:   Wed Jul 8 15:43:28 2015 +0300

    java: improved resolve of inner classes by JVM internal name

    Fixes navigation to inner classes with a dollar sign in a name from test console and debugger.