commit fcbdf999a15039ecad6157c1bf44a835df826a27
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Wed Nov 26 02:37:35 2008 +0000

    Pretty massive rewrite of PrivateModules so that they're now implemented in core Guice.

    This change introduces InjectorShell, an unfortunate class to hold an in-progress injector while it's being constructed. It refactors InjectorBuilder to support building several injectors simultaneously.

    Still outstanding is fixing up the docs for PrivateModule and these new APIs.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@701 d779f126-a31b-0410-b53b-1d3aecad763e