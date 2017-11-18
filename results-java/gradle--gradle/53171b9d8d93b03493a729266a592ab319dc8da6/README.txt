commit 53171b9d8d93b03493a729266a592ab319dc8da6
Author: Cedric Champeau <cedric@gradle.com>
Date:   Tue Jul 18 16:46:32 2017 +0200

    Don't leak the container of dependencies to the dependency itself

    This refactors the code a bit to avoid leaking the dependency container
    (`DefaultDependencySet`) to the dependency itself (`ModuleDependency`).
    The leakage happens because an anonymous inner class keeps a reference
    onto its containing class. To avoid this, we need to create a static
    inner class.