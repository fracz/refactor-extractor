commit a93e56c58af2611650d1f97190ffe54782479423
Author: Matthew Grooms <mgrooms@pfsense.org>
Date:   Fri Jul 11 01:55:30 2008 +0000

    Overhaul IPsec related code. Shared functions have been consolidated into
    a new file named /etc/ipsec.inc. Tunnel definitions have been split into
    phase1 and phase2. This allows any number of phase2 definitions to be
    created for a single phase1 definition. Several facets of configuration
    have also been improved. The key size for variable length algorithms can
    now be selected and the phase1 ID options have been extended to allow for
    more flexible configuration. Several NAT-T related issues have also been
    resolved.

    Please note, IPsec remote access functionality has been temporarily
    disabled. An improved implementation will be included in a follow up
    commit.