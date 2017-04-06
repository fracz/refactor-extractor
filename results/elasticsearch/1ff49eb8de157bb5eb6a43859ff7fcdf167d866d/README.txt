commit 1ff49eb8de157bb5eb6a43859ff7fcdf167d866d
Author: Ryan Ernst <ryan@iernst.net>
Date:   Tue Sep 1 11:44:52 2015 -0700

    Settings: Remove environment from transport client

    Transport clients run embedded within external applications, so
    elasticsearch should not be doing anything with the filesystem, as there
    is not elasticsearch home.

    This change makes a number of cleanups to the internal API for loading
    settings and creating an environment. The loadFromConfig option was
    removed, since it was always true except for tests. We now always
    attempt to load settings from config a file when an environment is
    created. The prepare methods were also simplified so there is now
    prepareSettingsAndEnvironment which nodes use, and prepareSettings which
    the transport client uses. I also attempted to improve the tests, but
    there is a still a lot of follow up work to do there.

    closes #13155