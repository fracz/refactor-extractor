commit 1d7166d19df9dc955c82f0459560de216c2432df
Author: Rodrigo B. de Oliveira <rodrigo@gradle.com>
Date:   Mon Jan 30 19:19:12 2017 -0200

    Polish `ExtensionsStorage`

     - Favor existing method over duplication
     - Favor null checking over exception-based control-flow
     - Favor package-private access over public
     - Handle unchecked casts via `Cast#uncheckedCast`
     - Remove unused method
     - Favor early return on not-null condition
     - Improve field declaration readability by introducing empty line
     - Make exception messages readable under 140 columns