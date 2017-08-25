commit 378b3eac8342a932e8ec04aa9969328115542948
Author: Dan Poltawski <dan@moodle.com>
Date:   Fri May 31 13:10:00 2013 +0800

    MDL-36316 useragent: Reduce user agent sniffing

    * core_useragent: Introduce is_vendor methods to improve readability
      when we are just detecting if a browser is from a vendor, rather than
      the exact version

    * Remove uncessary browser version checks when we are just detecting
      vendors - this makes the intention of our sniffing clearer.

    * Remove sniffing for browsers which we do not support, grades/ajax/tinymce
      all support modern browsers so there is no need to sniff for them.