commit dcc936601e174ca729162f33f9c1f7b508dfad73
Author: Mark Story <mark@mark-story.com>
Date:   Sat May 9 22:18:45 2015 -0400

    Add __debugInfo() methods to more objects.

    Many of these objects can contain Closures, and I'd like to improve how
    they are printed in DebugKit. This feels like the best way to improve
    developer experience around these objects without making the solution
    too specific to DebugKit.

    Refs cakephp/cakephp#6504