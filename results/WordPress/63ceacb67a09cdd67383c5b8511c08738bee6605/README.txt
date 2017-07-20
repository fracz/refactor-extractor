commit 63ceacb67a09cdd67383c5b8511c08738bee6605
Author: Jeremy Felt <jeremy.felt@gmail.com>
Date:   Sat Mar 7 04:40:28 2015 +0000

    Improve experience when deleting users from a multisite network.

    When deleting a user who is not associated with any sites, the current messaging can be confusing as only users associated with at least one site actually appear on the confirmation page for deletion.

    This experience can be improved by showing all users being deleted as well as their current site associations.

    * If an empty array of users is passed, don't attempt to confirm deletion.
    * If one user is passed, show a message crafted for a user of one.
    * If multiple users are passed, show a message crafted for many.
    * Show the pending results of all users to be deleted.
    * Update messaging around the deletion/confirmation process to be less misleading.

    Props Idealien, HarishChaudhari, DrewAPicture.

    Fixes #18132.

    Built from https://develop.svn.wordpress.org/trunk@31656


    git-svn-id: http://core.svn.wordpress.org/trunk@31637 1a063a9b-81f0-0310-95a4-ce76da25c4cd