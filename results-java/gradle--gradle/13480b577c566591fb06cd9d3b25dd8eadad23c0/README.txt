commit 13480b577c566591fb06cd9d3b25dd8eadad23c0
Author: Daz DeBoer <daz@gradle.com>
Date:   Wed May 11 20:24:05 2016 -0600

    Extricated the external `ComponentResolveMetadata` from Ivy ModuleDescriptor (mostly)

    This change is part of a larger refactor to remove the use of Ivy `ModuleDescriptor`
    as the backing storage for our various component metadata types. More to come.