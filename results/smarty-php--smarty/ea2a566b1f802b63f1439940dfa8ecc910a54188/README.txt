commit ea2a566b1f802b63f1439940dfa8ecc910a54188
Author: Uwe Tews <uwe.tews@googlemail.com>
Date:   Sat Mar 28 03:58:08 2015 +0100

    - bugfix Smarty_Security->allow_constants=false; did also disable true, false and null (change of 16.03.2015)
      - improvement added a whitelist for trusted constants to security Smarty_Security::$trusted_constants (forum topic 25471)