commit d6a799ad4af81da8840fbd5536efd49b9225ef8a
Author: Sam Brannen <sam@sambrannen.com>
Date:   Mon Feb 16 20:25:57 2015 +0100

    Preserve ordering of inlined props in @TestPropertySource

    The initial implementation for adding inlined properties configured via
    @TestPropertySource to the context's environment did not preserve the
    order in which the properties were physically declared. This makes
    @TestPropertySource a poor testing facility for mimicking the
    production environment's configuration if the property source mechanism
    used in production preserves ordering of property names -- which is the
    case for YAML-based property sources used in Spring Boot, Spring Yarn,
    etc.

    This commit addresses this issue by ensuring that the ordering of
    inlined properties declared via @TestPropertySource is preserved.
    Specifically, the original functionality has been refactored. extracted
    from AbstractContextLoader, and moved to TestPropertySourceUtils where
    it may later be made public for general purpose use in other frameworks.

    Issue: SPR-12710