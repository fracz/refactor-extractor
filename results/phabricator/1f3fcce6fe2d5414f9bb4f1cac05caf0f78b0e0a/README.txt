commit 1f3fcce6fe2d5414f9bb4f1cac05caf0f78b0e0a
Author: epriestley <git@epriestley.com>
Date:   Tue Dec 6 03:50:05 2016 -0800

    Provide a cached class map query for making key-based class lookups more efficient

    Summary:
    Ref T11954. Depends on D16993. We have a couple of "look up the class for this key" queries which are costly enough to show up on a profile.

    These aren't huge wins, but they're pretty easy. We currently do this like this:

    ```
    $class_map = load_every_subclass();
    return idx($class_map, $key);
    ```

    However, we don't need to load EVERY subclass if we're only looking for, say, the Conduit method subclass which implements `user.whoami`. This allows us to cache that map and find the right class efficiently.

    This cache is self-validating and completely safe even in development.

    Test Plan:
      - Used `curl` to make queries to `user.whoami`, verified that content was identical before and after the change.
      - Used `ab -n100` to roughly measure 99th percentile time, which dropped from 74ms to 65ms. This is a small improvement (13% in the best case, here) but it benefits every Conduit method call.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11954

    Differential Revision: https://secure.phabricator.com/D16994