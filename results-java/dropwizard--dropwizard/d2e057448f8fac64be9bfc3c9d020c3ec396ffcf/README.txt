commit d2e057448f8fac64be9bfc3c9d020c3ec396ffcf
Author: Coda Hale <coda.hale@gmail.com>
Date:   Tue Jun 5 10:45:09 2012 -0700

    Stop cloning resource arrays in AssetServlet.

    Closes #108.

    The ServletOutputStream is unlikely to modify the array, and not copying it should improve performance for larger assets.