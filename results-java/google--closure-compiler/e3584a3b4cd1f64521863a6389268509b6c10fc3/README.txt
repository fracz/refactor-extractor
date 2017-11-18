commit e3584a3b4cd1f64521863a6389268509b6c10fc3
Author: Dimitris Vardoulakis <dimvar@google.com>
Date:   Tue Dec 10 16:45:23 2013 -0800

    Add option preferStableNames which disables temporary renaming of
    local variables (eg, 'L 123') if the enclosing scope contains a
    large number of local variables.  This improves the stability of
    renaming when reusing rename maps.

    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=57933116