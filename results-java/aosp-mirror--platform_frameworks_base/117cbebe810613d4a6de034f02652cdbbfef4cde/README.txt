commit 117cbebe810613d4a6de034f02652cdbbfef4cde
Author: Raph Levien <raph@google.com>
Date:   Mon Aug 25 13:47:16 2014 -0700

    New weight-aware font config

    Parse new fonts.xml config file, and resolve weight selection based on
    the base weight of the font (as defined by a weight alias specified in
    the config file) and the requested bold flag. This change improves the
    appearance of bold spans for alternate weights of Roboto.

    In addition, this patch enables weight selection for fallback fonts.
    For example, if an additional font with a weight of 100 is added to the
    Hebrew font family in the fallback list, then requesting
    "sans-serif-thin" would select that font for Hebrew text.

    Bug: 14538154
    Change-Id: I99a04fad4f7bf01c75726e760d42735dd9003496