commit 9a56deb2833cfc72f259af5fb6c849feb46e729b
Author: Sam Brannen <sam@sambrannen.com>
Date:   Fri Jun 17 21:49:06 2011 +0000

    [SPR-8386] SmartContextLoader enhancements:
    - introduced processContextConfigurationAttributes() method in SmartContextLoader SPI
    - refactored AnnotationConfigContextLoader, AbstractContextLoader, AbstractGenericContextLoader, ContextLoaderUtils, and TestContext implementations to take advantage of the SmartContextLoader SPI, MergedContextConfiguration, and ContextConfigurationAttributes
    - deleted ResourceTypeAwareContextLoader
    - deleted ContextLoaderUtils.LocationsResolver and implementations
    - moved context key generation from TestContext to MergedContextConfiguration