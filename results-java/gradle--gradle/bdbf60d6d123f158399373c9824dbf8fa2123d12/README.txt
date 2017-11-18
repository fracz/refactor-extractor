commit bdbf60d6d123f158399373c9824dbf8fa2123d12
Author: Daz DeBoer <daz@gradle.com>
Date:   Sun Aug 21 14:52:23 2016 -0600

    Provide a basic service to query the type of executing build

    The `BuildTypeAttributes` services can be accessed to determine if a build
    is a composite build or a 'nested' build.

    The current implementation is very basic, using mutable flags set by other services
      on load. Should improve this to read available state where possible.