commit 2db6a92a51b8efd620fbc26879f69dc2e6e40bce
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Apr 14 16:30:38 2014 +0200

    improve error message for common elasticsearch discovery problems

    guess default HTTP endpoint for elasticsearch and try to read config

    * compare cluster.name
    * check compatible version
    * warn if no connection could be made

    fixes #506