commit cf4e7548a87a34a380f2472d38cf3622df594979
Author: Inaki <iarenuno@eteo.mondragon.edu>
Date:   Sat Apr 10 00:19:22 2010 +0000

    filter tex: MDL-10197 Quality improvement of TeX-images by using PNG format.

    It seems some TeX installs use a convert utility that renders poor quality
    images when using the GIF format, but render good ones when using PNG. If
    all the needed tools are available, let the admin choose the preferred
    output format.

    Merged from MOODLE_19_STABLE