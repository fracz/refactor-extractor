commit 9c0f445edb3d9a5a94d6a2208b4d935b61e55d9e
Author: Adam Murdoch <adam@gradle.com>
Date:   Thu Apr 28 14:39:43 2016 +1000

    Skip the Gradle classes that dispatch property and method calls when attempting to determine where a deprecated method or property is being used to let the user know.

    This improves the likelihood that the location can be found.