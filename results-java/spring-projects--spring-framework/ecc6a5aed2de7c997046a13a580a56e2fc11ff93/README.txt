commit ecc6a5aed2de7c997046a13a580a56e2fc11ff93
Author: Phillip Webb <pwebb@vmware.com>
Date:   Thu Jun 21 17:32:33 2012 -0700

    Improve SimpleStreamingClientHttpRequest performance

    Ensure that NonClosingOutputStream calls with a byte array call the
    corresponding methods of the underlying OutputStream rather than
    relying on the default NonClosingOutputStream implementation, which
    writes one bte at a time. This significantly improves performance.

    Issues: SPR-9530