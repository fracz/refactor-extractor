commit 59c88eb3c0611aac802e162d53503ce71864baef
Author: Sam Brannen <sam@sambrannen.com>
Date:   Mon Mar 21 12:53:14 2016 +0100

    Support @Cache* as merged composed annotations

    Prior to this commit, @Cacheable, @CacheEvict, @CachePut, and @Caching
    could be used to create custom stereotype annotations with hardcoded
    values for their attributes; however, it was not possible to create
    composed annotations with attribute overrides.

    This commit addresses this issue by refactoring
    SpringCacheAnnotationParser to use the newly introduced
    findAllMergedAnnotations() method in AnnotatedElementUtils. As a
    result, @Cacheable, @CacheEvict, @CachePut, and @Caching can now be
    used to create custom composed annotations with attribute overrides
    configured via @AliasFor.

    Issue: SPR-13475