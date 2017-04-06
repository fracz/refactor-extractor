commit b5f7970be5950580bde4de0002a578daf3ae3aac
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Aug 12 17:00:50 2014 -0700

    perf($compile): don't register $destroy callbacks on element-transcluded nodes

    This is a major perf win in the large table benchmark (~100ms or 9).

    This cleanup is needed only for regular transclusion because only then the DOM hierarchy doesn't match scope hierarchy
    (transcluded scope is a child of the parent scope and not a child of the isolate scope)

    We should consider refactoring this further for the case of regular transclusion
    and consider using scope events instead.