commit 21536f64e1f2ababb0e83478b3d961b89956267a
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri May 6 10:52:26 2016 +0200

    Polish info contributor feature

    This commit improves the `InfoContributor` infrastructure as follows:

    * `InfoEndpoint` no longer breaks its public API and returns a Map as
    before
    * `Info` is now immutable
    * All properties of the build are now displayed. Since we control the
    generation of that file, there is no longer a mode to restrict what's
    shown
    * Build info is now generated in `META-INF/build-info.properties` by
    default

    Closes gh-5734