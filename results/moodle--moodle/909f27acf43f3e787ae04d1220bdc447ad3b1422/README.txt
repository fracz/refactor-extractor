commit 909f27acf43f3e787ae04d1220bdc447ad3b1422
Author: Jerome Mouneyrac <jerome@mouneyrac.com>
Date:   Tue Feb 12 11:15:15 2013 +0800

    MDL-37410 External PHPunit test: used ->getDataGenerator()->enrol_user instead to manually enrol the user. + improve the unassignUserCapability to remove a capability assigned by getDataGenerator->enrol_users