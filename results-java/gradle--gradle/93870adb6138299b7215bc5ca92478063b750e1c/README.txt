commit 93870adb6138299b7215bc5ca92478063b750e1c
Author: Paul Merlin <paul@gradle.com>
Date:   Thu Nov 5 11:26:23 2015 +0100

    Internal properties are now hidden from report if not present in public

    This commit moves `InstanceFactory.getHiddenProperties()` method
    responsibility into a better place: `FactoryBasedNodeInitializer`.

    Along the way, setting the `hidden` flag on nodes for other usecases has
    been improved: no more need for a dedicated rule
    (see `ProjectionBackedModelRegistration`), it is now done in
    `DefaultModelRegistry.registerNode()`.

    Only ComponentSpec is tested for now, tests for BinarySpecs and
    LanguageSourceSets to come.

    +review REVIEW-5679