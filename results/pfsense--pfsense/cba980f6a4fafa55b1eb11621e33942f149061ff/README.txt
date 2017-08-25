commit cba980f6a4fafa55b1eb11621e33942f149061ff
Author: jim-p <jimp@pfsense.org>
Date:   Sun Sep 16 19:30:27 2012 -0400

    Add support for multiple DHCP pools within the interface's subnet, and allow most of the settings for the main range to be set specific inside the pool. (e.g. it allows setting different gateways and DNS for different pools). Still needs improved input validation to prevent overlapping ranges/pools.