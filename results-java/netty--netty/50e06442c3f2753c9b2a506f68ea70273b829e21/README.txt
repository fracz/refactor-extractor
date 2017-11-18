commit 50e06442c3f2753c9b2a506f68ea70273b829e21
Author: Scott Mitchell <smitchel@akamai.com>
Date:   Thu Sep 18 19:04:35 2014 -0400

    Backport header improvements from 5.0

    Motivation:
    The header class hierarchy and algorithm was improved on the master branch for versions 5.x. These improvments should be backported to the 4.1 baseline.

    Modifications:
    - cherry-pick the following commits from the master branch: 2374e17, 36b4157, 222d258

    Result:
    Header improvements in master branch are available in 4.1 branch.