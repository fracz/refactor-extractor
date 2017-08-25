commit 833feeae22f186203f77edf45d8e7b0336af38ef
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Tue Jul 19 18:04:16 2016 +0200

    Polish it off.

    * Fix wrong error code for MethodNameInvalid.
    * Make sure suggested alternative names don't start or end with an underscore.
    * Add a number of unit tests for:
            - PHP magic methods (now handled via parent)
            - SoapClient methods
    * Minor documentation fixes.
    * Minor improvement of the MethodNameInvalid error message.