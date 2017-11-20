commit 78f797dda84eee8f2a0d29e0c47970eaff0f1070
Author: Jesse Vincent <jesse@fsck.com>
Date:   Fri Oct 8 05:09:38 2010 +0000

    A previous refactoring broke notification for messages from the user by
    replacing  a localized string with substitutions with a concatenation
    in one of two places the string was used.