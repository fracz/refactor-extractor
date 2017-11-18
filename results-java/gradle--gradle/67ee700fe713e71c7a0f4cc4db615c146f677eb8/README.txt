commit 67ee700fe713e71c7a0f4cc4db615c146f677eb8
Author: Daz DeBoer <daz@gradle.com>
Date:   Wed Nov 23 19:17:18 2016 -0700

    Extract 'type' attribute within `ArtifactTransformer`

    This pushes the knowledge of the 'artifact type' attribute down into
    the `ArtifactTransformer`, where it is used.

    Further improvements will allow us to:
    - Allow more generic artifact selection based on a range of attributes.
    - Express artifact transformations in terms of artifact attributes, and
      not in terms of 'format'.
    - Use strongly-typed attributes for core artifact attributes (type,
      extension, etc). At the moment these are quite difficult to use in the
      DSL, so I've left them as string attributes from the consumer point of
      view.