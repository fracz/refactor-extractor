commit 3b044ab5c0e77d657dab8873f94ee3fe10e65d13
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Jan 15 22:14:00 2013 +0100

    Dependency resolve rule can substitute module id.

    1. In addition to 'version', it is now possible to manipulate module 'group' and/or 'name' via the dependency resolve rule.
    2. It is a first stab at the implementation - simplest working solution. Some things have refactoring pending. Some stuff needs Adam's feedback (changes to the DependencyGraphBuilder and reflective construction of the DefaultDependencyIdentifier).