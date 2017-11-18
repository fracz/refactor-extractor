commit 87584a8cd7949cbdef695f2289fd7b83698a410c
Author: Kristina Chodorow <kchodorow@google.com>
Date:   Thu Aug 6 15:14:19 2015 +0000

    Add output_dir option and improve repository handling

    I finally worked out how to get a simple test maven repository working, so
    added back integration testing.

    This removes the dependency on Aether and just uses the maven model lib to
    resolve dependencies, which seems to work better in the face of custom repositories.

    --
    MOS_MIGRATED_REVID=100031414