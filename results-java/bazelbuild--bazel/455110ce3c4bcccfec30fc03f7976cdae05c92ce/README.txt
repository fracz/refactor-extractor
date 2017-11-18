commit 455110ce3c4bcccfec30fc03f7976cdae05c92ce
Author: Googler <noreply@google.com>
Date:   Sun Jul 16 04:23:16 2017 +0200

    ResourceFilter supports dynamically configured resource filtering

    Dynamically Configured Resource Filtering change 3/6

    Resource filtering behaves somewhat differently when dynamically configured.
    Resources obtained from dependencies will have already been filtered and do not
    need to be filtered again. Resources that were filtered out do not need to be
    tracked since resource processing will never receive a reference to them
    anyway.

    Also, to make builds where ResourceFilter is dynamically configured, add equals
    and hashCode methods (otherwise, the configuration code throws a
    NullPointerException) and a global output prefix (otherwise, conflicts can
    occur).

    To ensure that the global output prefix (and the ResourceFilter object itself)
    is the same regardless of the ordering of filters in the android_binary, build
    the filters into a set, not a list, and sort them as part of creating the
    ResourceFilter object. This way, for example, objects built with filters
    "en,fr", "fr,en", and "en,en,fr" will all end up equal.

    Finally, rename the getFilteredResources method to better reflect its new role,
    and improve the isPrefiltering method to not try to prefilter if there are no
    filters.

    Add tests for all of this, and helper methods for all of those tests, including,
    most notably, a makeResourceFilter method that instantiates a ResourceFilter
    similarly to how it is actually created outside of tests (rather than directly
    calling the constructor) and a fake implementation of AttributeMap to support
    this.

    RELNOTES: none
    PiperOrigin-RevId: 162099178