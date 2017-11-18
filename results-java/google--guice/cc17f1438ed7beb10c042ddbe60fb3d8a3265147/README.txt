commit cc17f1438ed7beb10c042ddbe60fb3d8a3265147
Author: sberlin <sberlin@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Sun Feb 27 00:02:03 2011 +0000

    significantly improve error reporting for binding a key already bound in a child injector or private module.  include all sources in the error msg (since it can be in many sibling private modules or child injectors), including whether or not it as a JIT binding.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@1508 d779f126-a31b-0410-b53b-1d3aecad763e