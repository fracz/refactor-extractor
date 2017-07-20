commit 6804733ddcb9fcf72b2df3f796a0d5b228bb0ab2
Author: mattab <matthieu.aubry@gmail.com>
Date:   Fri May 24 20:01:03 2013 +1200

    Adding new DevicesDetection plugin, developed by Clearcode and sponsored by a client of Piwik Professional Services. A beautiful work of engineering, all released under GNU/GPL license!

    Fixes #3612
     * when enabled, the plugin will add a new submenu: Visitors> Devices
     * the new devices report contains NEW reports:
      * Much improved Device type (tracking 'car browser', 'console', 'desktop', 'feature phone', 'smartphone', 'tablet', 'tv')
      * Device brand (tracking more than 100 brands such as Nokia, Nintendo, Lenovo or Apple.
      * Device model (tracking hundreds of phone/console models)
      * Operating System versions (tracking 70 variations of operating systems including Ubuntu vs Kubuntu vs Debian vs Lubuntu vs Xubuntu)
      * Operating System families (Android vs Google TV vs Windows vs Windows mobile vs Mobile gaming consoles)
      * Browser versions
      * Browser families

    Refs #3505 There is some basic TV detection included and maybe you can help contribute better detections (see the .yml data files containing the regular expressions in YAML format)

    HOW DOES IT WORK

    This is quite beautiful system. It is a plugin that disabled by default. when enabled, it will create additional columns in the DB. Also at tracking, it will look at the user agent, and try to match it against one browser we know in the databases. The databases of user agent matching are composed by 3 YML files, parsed by spyc.php into php array.

    These 3 YML took dozens of hours of work and testing with dozens of mobile phones and devices for accuracy. We are happy with the result as they should cover > 80% of the devices commonly used. We hope the community will help us build up these YML files and make them better, so we can track accurately 90% or 95% of requests.

    The performance overhead is pretty small, but parsing the YML files + running dozens of regex will add some overhead. This is why it is still disabled by default. We will think about how to integrate it in core, in the next few months.

    Please let me know if you find any problem with this new awesome code!