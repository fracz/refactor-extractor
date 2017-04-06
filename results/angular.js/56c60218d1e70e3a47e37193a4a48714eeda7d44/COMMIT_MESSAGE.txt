commit 56c60218d1e70e3a47e37193a4a48714eeda7d44
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Tue May 27 15:11:45 2014 -0700

    fix($compile): bound transclusion to correct scope

    Nested isolated transclude directives.

    This improves/fixes the fix in d414b787173643362c0c513a1929d8e715ca340e.

    See the changed ng-ifunit test: The template inside ng-if should be bound to the
    isolate scope of `iso` directive (resp. its child scope). Not to a child of
    the root scope. This shows the issue with ng-if. It’s however problem with
    other directives too.

    Instead of remembering the scope, we pass around the bound parent transclusion.