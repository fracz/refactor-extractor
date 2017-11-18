commit 86142556d360048671dc129f7b0c316f542473af
Author: sberlin <sberlin@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Sat Jun 25 05:05:23 2011 +0000

    refactor removeSuppressedTests out of AllTests, to remove the circular dependency between it & StrictContainerTestSuite.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@1557 d779f126-a31b-0410-b53b-1d3aecad763e