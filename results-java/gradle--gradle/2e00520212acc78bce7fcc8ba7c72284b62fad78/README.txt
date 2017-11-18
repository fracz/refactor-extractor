commit 2e00520212acc78bce7fcc8ba7c72284b62fad78
Author: Daz DeBoer <daz@gradle.com>
Date:   Thu Mar 2 12:59:25 2017 -0700

    Split visitor from results when collecting file dependencies

    This refactor moves file dependency collection slightly closer to
    regular artifact collection when visiting the resolved dependency graph.