commit 2ba9a8761875fc1061d74ec699ff79ce4c4df95a
Author: Steve Clay <steve@mrclay.org>
Date:   Wed Mar 9 18:28:02 2016 -0500

    deprecate(db): deprecates many methods on the `Application::getDb` object

    This low-level API no longer returns the active `Elgg\Database`, but instead
    an `Elgg\Application\Database` proxy object. Many of the methods on that
    object are deprecated as they were never meant to be public.

    This returns `Elgg\Database` to private API so we may refactor it as needed.