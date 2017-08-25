commit a116b3e7f4390f781a59fb684ea5a30924cca85b
Author: Andy Fragen <andy@thefragens.com>
Date:   Wed May 25 19:13:53 2016 -0700

    refactor `get_remote_{plugin|theme}_meta()`

    to `get_remote_repo_meta()`
    The other methods were present in some fashion in 4 different places. Now much less code. :)