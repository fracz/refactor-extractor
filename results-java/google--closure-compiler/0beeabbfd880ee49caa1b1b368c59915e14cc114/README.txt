commit 0beeabbfd880ee49caa1b1b368c59915e14cc114
Author: johnlenz <johnlenz@google.com>
Date:   Thu Aug 4 14:51:40 2016 -0700

    Switch "length" custom prop of Node to a class field.  This saves a basic object when for every node in IDE mode and makes the length available everwhere so we can improve error message and such things.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=129376537