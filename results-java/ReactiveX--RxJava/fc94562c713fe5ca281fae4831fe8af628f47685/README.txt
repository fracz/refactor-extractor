commit fc94562c713fe5ca281fae4831fe8af628f47685
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Mon Oct 6 09:32:11 2014 -0700

    ObserveOn Cleanup

    - make code easier to understand with some refactoring of the pollQueue loop
    - add a unit test to ensure correct behavior with a hot Observable source