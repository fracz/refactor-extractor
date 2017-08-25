commit fe53416e4f1a989bbce50b052f7f1bf6c55d24cc
Author: pmeenan <pmeenan@webpagetest.org>
Date:   Thu Jul 18 16:32:03 2013 -0400

    Order of magnitude performance improvement for resultimage.php (from 6ms to 0.4ms on the production webpagetest instance).  It was ~20% of the CPU time for the server and is now down to <2%