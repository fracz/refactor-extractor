commit cc530b9037a8fbb016f3a026d072381ccab18372
Author: Adrien Grand <jpountz@gmail.com>
Date:   Tue May 13 09:11:12 2014 +0200

    Use t-digest as a dependency.

    Our improvements to t-digest have been pushed upstream and t-digest also got
    some additional nice improvements around memory usage and speedups of quantile
    estimation. So it makes sense to use it as a dependency now.

    This also allows to remove the test dependency on Apache Mahout.

    Close #6142