commit cf6e762cbc9f2bedf7a0d3ed1deb2cf6a02e1cdd
Author: Marc Würth <ravage@bluewin.ch>
Date:   Thu Jun 15 11:18:08 2017 +0200

    Improve readability

    Since we are not writing back into the config array, we do not need to work on the $config variable the whole time.