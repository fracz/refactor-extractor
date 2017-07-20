commit f1dd39d5e20231119645f774ab07816dcb9d8062
Author: Serban Ghita <serbanghita@gmail.com>
Date:   Wed Sep 19 00:35:09 2012 +0300

    - added new method $detect->version() which detects the version of certain components. It's purpose is to establish a connection between the versions of the mobile OS, mobile browser and the device. It can be helpful to
    - added new method $detect->mobileGrade() which is based on jQuery Mobile Supported Platforms http://jquerymobile.com/demos/1.2.0-rc.1/docs/about/platforms.html
    - #81:  Achos device not recognized as tablet
    - #82:  Chrome is detected as Safari
    - #83:  Currently detects google nexus tablet as phoneand is not valid as tablet
    - #87:  Include differentiation betweeen Windows CE & Windows Mobile
    - #89:  Chrome for iOS
    - #92:  Google Nexus 7 not detected as Tablet
    - many fixes, improved speed when using batch mode
    - deprecated $userAgent and $httpHeaders params from $detect->is() and $detect->isMobile() & friends.