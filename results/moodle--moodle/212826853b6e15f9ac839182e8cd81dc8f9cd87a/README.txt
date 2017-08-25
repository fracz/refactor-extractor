commit 212826853b6e15f9ac839182e8cd81dc8f9cd87a
Author: Petr Skoda <commits@skodak.org>
Date:   Sun Jun 10 16:28:31 2012 +0200

    MDL-33635 improve collatorlib

    1/ the sort flag can not use Intl extension constants because they may not be available
    2/ add sort flag to all methods
    3/ use private constructor instead of abstract - more Java like API to match the textlib
    4/ add natural sorting support
    5/ consistent bool return type
    6/ better non-intl fallback
    7/ more tests