commit 0b917e3f9ced6449fdc47a5fcae2105e94212c28
Author: Costin Leau <cleau@vmware.com>
Date:   Tue May 17 16:50:00 2011 +0000

    revise cache API
    - eliminate unneeded methods
    + introduced value wrapper (name still to be decided) to avoid cache race conditions
    + improved name consistency