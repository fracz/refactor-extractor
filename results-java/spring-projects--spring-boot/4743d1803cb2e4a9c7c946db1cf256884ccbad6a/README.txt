commit 4743d1803cb2e4a9c7c946db1cf256884ccbad6a
Author: Phillip Webb <pwebb@vmware.com>
Date:   Wed May 8 14:24:06 2013 -0700

    Update JavaLoggerConfigurer to use AC ClassLoader

    Update JavaLoggerConfigurer to use the ApplicationContext classloader
    rather then the default classloader. Also refactored to introduce
    LoggingSystem enum to act as a strategy for the specific logging
    systems.