commit 86cb3bb7d9b662398dc2b71433a9f6c6a6392b5c
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Wed Oct 15 21:25:50 2008 +0000

    Refinements to PrivateModules:
     - new test cases
     - support for @Provides methods
     - error detection on keys that are exposed but not bound

    Also refactoring ProviderMethods to make this work.

    I'm beginning to run into a wall with what is possible with implementing private modules as an extension rather than as a part of core. In particular:
     - I need to hack ProviderMethods so they're not installed in the public scope
     - I need to hack BindingProcessor to tolerate doubly-bound keys for Private Modules

    The lack of core-support is also going to prevent private modules from having first-class support in the SPI. Ideally we should be able to have a binding type called "ChildBinding" to which the exposed keys belong. This binding would simply point at the child injector that implements the binding (and possibly the delegate binding also).

    It's also preventing me from being able to detect a small class of errors - when a child module binds a key that's exposed by one of its sibling modules, we don't detect the binding conflict.


    git-svn-id: https://google-guice.googlecode.com/svn/trunk@633 d779f126-a31b-0410-b53b-1d3aecad763e