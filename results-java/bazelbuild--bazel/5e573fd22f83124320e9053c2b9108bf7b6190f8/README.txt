commit 5e573fd22f83124320e9053c2b9108bf7b6190f8
Author: Michael Staib <mstaib@google.com>
Date:   Wed Jan 27 00:33:29 2016 +0000

    Store data about aspect configurations on Dependencies.

    Dependencies are the data structure which needs to propagate the configuration for each
    aspect as created by trimConfigurations down to the point where it's actually used. We
    need this to store different configurations for different aspects in a world where aspects
    have their own configurations, which may have more fragments than the target they're
    attached to.

    That world is on its way.

    Also in this CL:
    * Refactor Dependency to be an abstract parent class with separate implementations for
      Attribute.Transitions and BuildConfigurations, as well as null configurations, to avoid
      having to check nullness in various places. Users of the API will not see this, but get
      factory methods instead of constructors. As a consequence of this, refactor Dependency
      to be its own top-level class instead of a nested class in DependencyResolver.

    --
    MOS_MIGRATED_REVID=113109615