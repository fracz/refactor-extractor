commit 22767b80af17c3fb82414b61be2c8d68a865758b
Author: epriestley <git@epriestley.com>
Date:   Mon Apr 11 02:24:39 2011 -0700

    Make files coming out of the Files tool cacheable, since this improves
    performance (e.g., for profile images) and you need to know a highly entropic
    PHID to access a file in the first place, plus installs should generally be
    doing HTTPS.