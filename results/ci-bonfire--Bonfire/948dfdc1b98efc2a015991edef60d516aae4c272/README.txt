commit 948dfdc1b98efc2a015991edef60d516aae4c272
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Wed Oct 22 11:25:56 2014 -0700

    Added bfmysqli driver to improve mysqli support

    Allows improved mysqli support without changing the CodeIgniter code.
    Simply set the database config as if you were configuring it for mysqli,
    but change the 'dbdriver' to 'bfmysqli'.