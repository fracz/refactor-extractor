commit 22b57ddd601c5022ee49b06ee79290471ad6fe84
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Fri Nov 4 14:33:16 2016 -0600

    Make ExtractionNamespaceCacheFactory to populate cache directly instead of returning callable (#3651)

    * Rename ExtractionNamespaceCacheFactory.getCachePopulator() to populateCache() and make it to populate cache itself instead of returning a Callable which populates cache, because this "callback style" is not actually needed.

    ExtractionNamespaceCacheFactory isn't a "factory" so it should be renamed, but renaming right in this commit would tear the git history for files, because ExtractionNamespaceCacheFactory implementations have too many changed lines. Going to rename ExtractionNamespaceCacheFactory to something like "CachePopulator" in one of subsequent PRs.

    This commit is a part of a bigger refactoring of the lookup cache subsystem.

    * Remove unused line and imports