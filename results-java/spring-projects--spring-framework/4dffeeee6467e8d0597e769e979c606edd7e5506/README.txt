commit 4dffeeee6467e8d0597e769e979c606edd7e5506
Author: Sam Brannen <sam@sambrannen.com>
Date:   Sun May 31 22:51:37 2015 +0200

    Introduce alias for 'value' attribute in caching annotations

    This commit introduces new 'cacheNames' attributes (analogous to the
    existing attribute of the same name in @CacheConfig) as aliases for the
    'value' attributes in @Cacheable, @CachePut, and @CacheEvict.

    In addition, SpringCacheAnnotationParser.getAnnotations() has been
    refactored to support synthesized annotations.

    Issue: SPR-11393