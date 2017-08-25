commit 5fe2604c3a3019908adb2bf3865e339dbff43a8e
Author: Todd Burry <todd@vanillaforums.com>
Date:   Tue Aug 2 16:56:11 2016 -0400

    Fix asset sorting

    The dispatcher refactor assigns a lowercased application name to the controller rather than the title cased one. This causes the asset sort arrays to not work. Rather that changing the application that’s assigned to the dispatcher I opted to make the asset sort containers case-insensitive.

    This PR also includes some variable renaming to be in line with code standards.