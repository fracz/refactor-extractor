commit b298fe95887ac547ae9f89a16c2a6e14f500dd6a
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Thu Jul 16 21:59:34 2015 +0300

    HyperLinks to files in console improved.

    I used latest ":", but I should use one from Regexp group, so in phrase
    "file.py:20 :Error" only "file.py:20" should be highlighted.