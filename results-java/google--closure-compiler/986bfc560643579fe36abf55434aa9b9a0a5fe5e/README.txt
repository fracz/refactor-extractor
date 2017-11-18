commit 986bfc560643579fe36abf55434aa9b9a0a5fe5e
Author: mknichel <mknichel@google.com>
Date:   Thu Aug 4 13:01:03 2016 -0700

    Fix a bug in RefasterJS that caused RefasterJS templates to match against the wrong code if the compilation unit that was being refactored does not contain a type used in the RefasterJS template.

    Since RefasterJS uses the same compiler object as whatever compilation unit it is refactoring, any type that is used by RefasterJS templates must already be known by that compilation unit or RefasterJS must not match against any code in that compilation unit. This previously didn't work because the template types were being treated as unknown, thereby loosely matching a lot of code.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=129363403