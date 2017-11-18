commit 07170cc3f59f08954c97e19f2d3cf165d3a8601a
Author: sberlin <sberlin@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Fri Mar 11 03:02:39 2011 +0000

    remove phases from BindingProcessor, refactor so that two different classes do the two different bits, with an abstract superclass managing the shared pieces.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@1527 d779f126-a31b-0410-b53b-1d3aecad763e