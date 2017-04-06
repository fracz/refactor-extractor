commit 02ce826ce4720bc6a74eaf3439656753fe6fcc44
Author: Phillip Webb <pwebb@vmware.com>
Date:   Thu Sep 13 16:38:59 2012 -0700

    Cache and late resolve annotations for performance

    Annotations are no longer resolved in the constructor and are cached
    for improved performance.

    Issue: SPR-9166