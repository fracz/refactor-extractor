commit f74b96423612a176661a029e7b9d66bfa2ccad5e
Author: Max Pumperla <max.pumperla@googlemail.com>
Date:   Tue Oct 24 10:29:21 2017 +0200

    Separable 2d convolution layer (#4187)

    * sep conv 2d config

    * sep conv 2d impl

    * tests

    * typo

    * starting keras integration

    * layer doc

    * sep conv param init

    * constraints for pointwise conv

    * clean up

    * regularization and shape fixes

    * import of sep conv keras weights

    * refactor weight regularizer to be more general

    * set depth-wise regularizer on keras model
    ;

    * fix tests and clean-up