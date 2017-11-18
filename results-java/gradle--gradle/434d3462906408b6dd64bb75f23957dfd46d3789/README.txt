commit 434d3462906408b6dd64bb75f23957dfd46d3789
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Aug 16 18:45:36 2012 +0200

    Dependency report, some refactoring.

    ResolvedDependencyResult now uses ModuleVersionSelector for the 'requested' property. Before it was using the ModuleVersionIdentifier to speed up initial development.