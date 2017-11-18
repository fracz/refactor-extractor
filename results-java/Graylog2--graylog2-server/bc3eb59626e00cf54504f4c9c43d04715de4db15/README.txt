commit bc3eb59626e00cf54504f4c9c43d04715de4db15
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Jun 12 18:21:54 2013 +0200

    slightly refactor Api class

    use ning's netty based async http client for API requests
    don't generate a new object where the class object is enough