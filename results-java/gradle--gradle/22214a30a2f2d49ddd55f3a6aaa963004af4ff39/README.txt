commit 22214a30a2f2d49ddd55f3a6aaa963004af4ff39
Author: Stefan Oehme <stefan@gradle.com>
Date:   Thu Oct 6 11:17:05 2016 +0200

    Run TAPI builders before buildFinished

    Until now, the TAPI builders were run after the build was already finished.
    This lead to build-scoped services no longer being available. Among these services
    is the dependency resolution cache, leading to a severe performance degradation
    on projects with new or changing dependencies.

    TAPI builders are now executed inside the build, using a buildFinished hook.
    This means that other buildFinished hooks will not yet have run when the model is
    built - a breaking change that we are willing to exchange for the huge performance
    improvement.