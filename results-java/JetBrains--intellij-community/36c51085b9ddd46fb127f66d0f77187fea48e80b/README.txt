commit 36c51085b9ddd46fb127f66d0f77187fea48e80b
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Wed Mar 30 15:23:51 2016 +0200

    WEB-20986 JSON Schema Files that are updated require a close and relauch to be used
    0) schema providers have externally returned identity key - in order to easier replace them in a cache (type of schema - embedded, schema itself, user defined + file)
    1) have vfs listener to track user schema files
    2) dependencies cache also to keep dependencies between schema provider identities (i.e. schema files) - in order for fine grained cache update
    3) refactor components to reduce cross calls
    4) remove explicit factory for tslint, use common one