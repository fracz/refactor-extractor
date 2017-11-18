commit 59f1d889f71d6d203ca061787d4ea16465df1916
Author: tomlu <tomlu@google.com>
Date:   Tue Aug 15 23:21:25 2017 +0200

    Add more type safety to CustomCommandLine.

    Allowing add(Object) is too loose and can easily lead to programmer mistakes.

    Because of type erasure, we can't use the same overload name for (eg.) add(NestedSet<String>) and add(NestedSet<Artifact>). The API is overhauled to use the same terms everywhere, eg. "add", "addPaths", "addExecPaths". This is similar to how it used to be a few CLs ago.

    The API is overhauled to make sure it's consistent for all types. While tedious, the facade methods immediately dispatch to internal helpers, so implementation wise it's not too heavy.

    While large, this CL is almost entirely an automated refactor.

    PiperOrigin-RevId: 165358287