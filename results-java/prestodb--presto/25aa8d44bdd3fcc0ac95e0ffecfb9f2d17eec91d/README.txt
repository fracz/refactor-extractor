commit 25aa8d44bdd3fcc0ac95e0ffecfb9f2d17eec91d
Author: Martin Traverso <martint@fb.com>
Date:   Fri Feb 27 12:22:22 2015 -0800

    Fix NPE in MetadataQueryOptimizer

    The PlanRewriter refactoring in b4eaa84b769c2ef7cc585977e59dc418d2c9035e missed a couple of changes
    to MetadataQueryOptimizer that result in NullPointerExceptions