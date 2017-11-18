commit 072a729856f270c2fd7a8e53001a267163a7ea0d
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Thu Oct 18 23:53:58 2012 +0200

    saner AbstractOptions implementation

    - easier to leverage from Java, less wasteful
    - refactored all AbstractOptions subclasses accordingly
    - no longer converting field names to ant property names if they only differ in case (doesn't matter to Ant)
    - simplified some tests on the go