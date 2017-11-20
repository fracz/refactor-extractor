commit 8f0e6bd5f0ce681e72cb81e096a207292eb05877
Author: Havoc Pennington <hp@pobox.com>
Date:   Fri Nov 11 15:59:38 2011 -0500

    Make merging work properly with substitutions involved.

    If we saw a ConfigSubstitution value, we would then
    ignore any objects after it. But in fact, the substitution
    might expand to an object, and then we would need to merge
    it with the objects after it.

    If we had an object and merged a substitution with it, we
    were previously ignoring the substitution. But in fact,
    the substitution might expand to an object, and we would
    need to merge that object in.

    So in both cases now we create a ConfigDelayedMerge
    or ConfigDelayedMergeObject object instead.

    As part of this, the merge() code was refactored to
    use a withFallback() method, which is now handy
    and public.