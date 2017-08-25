commit 95a3bb4dc87dd45948f4517dc40de466e4f20e21
Author: Ankit Agarwal <ankit@moodle.com>
Date:   Fri Jun 14 11:35:12 2013 +0800

    MDL-39152 phpunit: improve tests for assign_roles()

    core_role_external::assign_roles() now support alternative paramaters instead of contextid. Test working of those. Also test the parameters validation a bit, since these are no longer automatically tested being optional now