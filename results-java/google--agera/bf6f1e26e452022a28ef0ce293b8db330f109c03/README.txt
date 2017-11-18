commit bf6f1e26e452022a28ef0ce293b8db330f109c03
Author: Magnus Ernstsson <magnus@ernstsson.net>
Date:   Sun May 29 09:10:28 2016 +0100

    Made a slight perf improvement of single repository (#55)

    Removed checkNotNull but kept the behaviour of throwing
    NullPointerException