commit 5b40eb48e08065d12bf0c3a1b727b29439171d05
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Mon Jan 2 19:28:49 2017 -0800

    Improve MBean without backing Endpoint support

    Improve support for MBeans without a backing endpoint by introducing
    a `JmxEndpoint` interface. The `JmxEndpoint` is intentionally
    similar in design to the `MvcEndpoint` from the `mvc` package and
    allows for completely custom JMX beans that are not backed by any
    real actuator `Endpoint`.

    The `AuditEventsMBean` has been refactored to use the new interface and
    has been renamed to `AuditEventsJmxEndpoint`.

    See gh-6579