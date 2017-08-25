commit ead8ba3b63cf67ddeac784e2bf8933a283347b3f
Author: David Mudrak <david@moodle.com>
Date:   Fri May 25 11:17:43 2012 +0200

    MDL-32329 improved plugin dependencies check on upgrade

    Previously, it was the renderer method that actually called
    all_plugins_ok(). I believe that renderer methods should not be
    responsible for such an important step in the install/upgrade code flow.
    So dependencies are now checked by admin/index.php on upgrade, too.