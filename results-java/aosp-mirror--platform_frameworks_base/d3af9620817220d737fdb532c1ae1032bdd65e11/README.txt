commit d3af9620817220d737fdb532c1ae1032bdd65e11
Author: Chad Brubaker <cbrubaker@google.com>
Date:   Mon Nov 16 10:48:20 2015 -0800

    Expose findTrustAnchorBySubjectAndPublicKey

    This allows for faster lookups of TrustAnchors when checking pin
    overrides without needing to iterate over all certificates.

    Currently only the system and user trusted certificate store are
    optimized to avoid reading the entire source before doing the trust
    anchor lookup, improvements to the resource source will come in a later
    commit.

    This also refactors System/UserCertificateSource to avoid code
    duplication.

    Change-Id: Ice00c5e047140f3d102306937556b761faaf0d0e