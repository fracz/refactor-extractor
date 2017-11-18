commit e74fc80aab1b1b24a4e775152613068d9500dc33
Author: olly <olly@google.com>
Date:   Thu May 26 03:43:17 2016 -0700

    Loader improvements.

    This change moves generally useful functionality (load timing
    and fatal error propagation) inside of Loader, so that callers
    don't have to duplicate effort.

    The change also makes use of generics so that the callback
    receives a Loadable with a more specific type.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=123304369