commit 163c48aafee02f9e918a6e7385f5b3312e8edfb6
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Mon Jun 16 02:58:08 2008 +0000

    Cleanup after the big errors refactoring.

    SourceProviders are now used a lot at bind time, but not afterwards, when the injector is actually being built. We might be able to make this class non-static if we attach it to the Binder.

    Renamed ResolveFailedException to ErrorsException.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@521 d779f126-a31b-0410-b53b-1d3aecad763e