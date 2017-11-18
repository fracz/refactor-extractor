commit 76c24b172e5c4c4b6d51c10dd5c1f491a4033157
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Thu Dec 25 04:32:41 2008 +0000

    This should be the last big refactoring before Guice 2 final...

    - Restored Bob's binding interfaces - LinkedBinding, InstanceBinding, etc.
    - Moved Binding implementation classes to internal/ so the implementations could  be shared by both Injector bindings and Module bindings
    - Added HasDependencies interface, implemented it for ProviderMethods
    - Cleaned up our internal model of scopes to use the 'Scoping' class, which is like scope annotation/instance + eager/lazy.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@739 d779f126-a31b-0410-b53b-1d3aecad763e