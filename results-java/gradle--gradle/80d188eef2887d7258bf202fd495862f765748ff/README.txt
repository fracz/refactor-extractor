commit 80d188eef2887d7258bf202fd495862f765748ff
Author: Stefan Oehme <stefan@gradle.com>
Date:   Mon Aug 29 15:07:01 2016 +0200

    Remove GradleConnection API

    The GradleConnection API was our first attempt at
    implementing composite builds. We have improved on that
    in Gradle 3.1, allowing the user to define composite builds
    in settings.gradle and giving the user much more control
    over how dependency substitution works.

    A composite build is a normal Gradle build as far as the
    Tooling API is concerned, so the separate concept of
    the GradleConnection is no longer needed. We will add
    methods for fetching all models from a composite build
    to ProjectConnection in Gradle 3.2