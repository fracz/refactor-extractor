commit 1cf5b0e4d3d50ba702360899550626dc1ac8b71a
Author: robocoder <anthon.pang@gmail.com>
Date:   Wed Feb 15 14:02:18 2012 +0000

    fixes #2934, refs #2921 - pre-concatenate & minify jqplot library and plugins

     * a single 164K file (yuicompressed) vs 460K spread over 18 files
     * this will improve initial Dashboard loading time by reducing the AssetManager's workload
     * deferring sync up with upstream jqplot to next milestone



    git-svn-id: http://dev.piwik.org/svn/trunk@5845 59fd770c-687e-43c8-a1e3-f5a4ff64c105