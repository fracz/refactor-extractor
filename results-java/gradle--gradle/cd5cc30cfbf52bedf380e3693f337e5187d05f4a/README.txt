commit cd5cc30cfbf52bedf380e3693f337e5187d05f4a
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri May 11 21:44:55 2012 +0200

    Providing external gradle module information in Tooling API: unresolved dependencies.

    For now, only for idea model. I tried to minimize the changes so that we don't overinvest the types that will go away (or get refactored) at some point soon (LenientConfiguration, UnresolvedDependency).