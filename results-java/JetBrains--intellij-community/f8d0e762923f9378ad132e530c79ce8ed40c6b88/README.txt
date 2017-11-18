commit f8d0e762923f9378ad132e530c79ce8ed40c6b88
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Mon Jun 8 23:09:46 2015 +0300

    Any python output in helpers now does not break manage py (PY-16083)
    * This commit also outputs errors directly in console and logs as well. It improves user experience.