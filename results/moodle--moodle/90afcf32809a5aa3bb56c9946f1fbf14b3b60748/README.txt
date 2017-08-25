commit 90afcf32809a5aa3bb56c9946f1fbf14b3b60748
Author: skodak <skodak>
Date:   Tue Aug 21 20:52:36 2007 +0000

    MDL-10260 added new user_delete() hook into auth plugins; refactored user delete code = new function delete_user() in moodlelib.php + improved cleanup in core tables when deleting user