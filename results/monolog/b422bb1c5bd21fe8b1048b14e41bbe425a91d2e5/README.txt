commit b422bb1c5bd21fe8b1048b14e41bbe425a91d2e5
Author: odino <alessandro.nadalin@gmail.com>
Date:   Wed Jul 24 10:14:35 2013 +0400

    refactored the implementation of the NR handler and its test:
    * the test now checks that if the extension is not loaded, an exception is thrown
    * the test now mocks the new relic native functions
    * moved some parameters as constants in the handler class