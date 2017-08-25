commit e2c6e8f7d274ff8935faa22ebb79bd42d74a519a
Author: Alan Jenkins <alan.christopher.jenkins@gmail.com>
Date:   Wed Sep 12 12:08:32 2012 +0100

    builder generating view_default: prep: refactor control group wrapper

    This commit is deliberately limited to moving (well, copying) code around.
    With the code moved together, we notice that the indentation is not
    quite right.  (Although that's a side-effect; the original motivation was
    to allow tweaking of the radio buttons in order to get valid HTML).