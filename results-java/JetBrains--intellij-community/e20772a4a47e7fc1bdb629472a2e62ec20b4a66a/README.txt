commit e20772a4a47e7fc1bdb629472a2e62ec20b4a66a
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Sep 21 16:21:31 2015 +0300

    [Patch]: IDEA-75177 Create Patch: 'file name length cannot exceed 100 chars' message  and  limit for patch creation improved

    * keep MAX limit for filename created from shelve manager;
    * It's not necessary to detect a file path length according to Windows restrictions in other places (shelf), because it may produce  unreasonably big amount of code for a minor case.