commit 04ca1b4676fa636138071da083f9159707d7328f
Author: Gábor Hojtsy <gabor@hojtsy.hu>
Date:   Mon Nov 19 13:56:14 2007 +0000

    #190283 by JirkaRybka and myself: fix installer localization and form handling
     - use a two pass localization process so localization is ready for the configure form and profile tasks
     - fix awkward form API workarounds which were introduced before we used a full bootstrap anyway
     - allow for more usable localized profiles by letting them skip language selection
     - lots of documentation improvements to profiles and the installer functions