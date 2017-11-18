commit e4d829ba1adb8346a594a51a1e17b388b4b6a58f
Author: Stefan Oehme <stefan@gradle.com>
Date:   Thu Mar 9 16:49:41 2017 +0100

    Extract interfaces for ResourceFilters

    We want to get rid of the Closure-taking methods, but that would require
    a bigger refactoring of the `EclipseProject` code to use the `Instantiator`
    everywhere. Instead of doing this shortly before a release, I decided to
    extract interfaces for the ResourceFilters (which will come in handy anyway
    when we introduce convenience subclasses). This way we hide the
    Closure-taking methods from the public API, making the easier to remove
    later on.