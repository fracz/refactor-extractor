commit c3aec364e6ce30213176bae3a3d0ec5b4cb37898
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Nov 26 21:19:43 2013 -0500

    Make Astyanax always assume Deployment.REMOTE

    This is a slight improvement over returning nothing and causing a
    compile error.