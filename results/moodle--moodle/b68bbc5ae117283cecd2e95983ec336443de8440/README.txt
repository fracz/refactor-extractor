commit b68bbc5ae117283cecd2e95983ec336443de8440
Author: David Mudrák <david@moodle.com>
Date:   Thu Dec 6 02:31:36 2012 +0100

    MDL-36963 Improve mdeploy worker::move_directory() method

    The additional parameter allows to use this method without actual
    removing the root of the source location. That is, it is now possible to
    move the content of a folder only.

    Also, a small refactoring happened here as we will need a variant of
    this method that does not throw exception if the target already exists.