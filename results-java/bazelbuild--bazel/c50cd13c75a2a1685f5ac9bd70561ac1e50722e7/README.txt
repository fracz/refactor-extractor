commit c50cd13c75a2a1685f5ac9bd70561ac1e50722e7
Author: ccalvarin <ccalvarin@google.com>
Date:   Mon Oct 30 07:04:07 2017 -0400

    Compute canonical list of options using OptionValueDescription's tracking of instances.

    Stop storing the canonical list of arguments separately. For the canonicalize-flags command, we want to avoid showing options that either have no values in their own right (such as expansion options and wrapper options) and instances of options that did not make it to the final value. This is work we already do in OptionValueDescription, so we can generate the canonical form from the values tracked there, instead of tracking it separately.

    This means the canonical list is more correct where implicit requirements are concerned: implicit requirements are not listed in the canonical form, but now the values they overwrote will be correctly absent as well.

    Use this improved list for the effective command line published to the BEP.

    RELNOTES: Published command lines should have improved lists of effective options.
    PiperOrigin-RevId: 173873154