commit a996ca71a90d677940cdbed5c13b97e6550165bb
Author: Luke Daley <ld@ldaley.com>
Date:   Mon Nov 19 14:27:10 2012 +0000

    POM encoding handling improvements.

    • Introduce a constant on MavenPom for the encoding
    • Read the POM with this encoding in the test
    • Use explicit unicode char notation in test data