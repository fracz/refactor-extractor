commit c2b56c07e59aeb123fbe63b282c0ef98d7380f56
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Mon Dec 2 21:55:00 2013 -0800

    Cache property TypeDescriptors

    Attempt to improve performance by caching TypeDescriptors against bean
    PropertyDescriptors in CachedIntrospectionResults.

    This change is an attempt to fix the failing performance test case
    `testPrototypeCreationWithOverriddenResourcePropertiesIsFastEnough` in
    `AnnotationProcessorPerformanceTests`.