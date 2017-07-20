commit 9dbf6cb21872aae5a77a71db34f29ea33eac8564
Author: Michal Čihař <mcihar@suse.cz>
Date:   Mon Sep 30 14:21:53 2013 +0200

    Update TCPDF to 6.0.036

    Full changelog:

    6.0.036 (2013-09-29)
            - Methods for registration bars and crop marks were extended to support registration color (see example n. 56).
            - New default spot colors were added to tcpdf_colors.php, including the 'All' and 'None' special registration colors.

    6.0.035 (2013-09-25)
            - TCPDF_PARSER class was improved.

    6.0.034 (2013-09-24)
            - Bug #839 "Error in xref parsing in mixed newline chars" was fixed.

    6.0.033 (2013-09-23)
            - Bug fix related to PNG image transparency using GD library.

    6.0.032 (2013-09-23)
            - Bug #838 "Fatal error when imagick cannot handle the image, even though GD is available and can" was fixed.

    6.0.031 (2013-09-18)
            - Bug #836 "Optional EOL marker before endstream" was fixed.
            - Some additional controls were added to avoid "division by zero" error with badly formatted input.

    6.0.030 (2013-09-17)
            - Bug #835 "PDF417 and Cyrilic simbols" was fixed.

    6.0.029 (2013-09-15)
            - Constants K_TCPDF_PARSER_THROW_EXCEPTION_ERROR and K_TCPDF_PARSER_IGNORE_DECODING_ERRORS where removed in favor of a new configuration array in the TCPDF_PARSER class.
            - The TCPDF_PARSER class can now be configured using the new $cfg parameter.

    6.0.028 (2013-09-15)
            - A debug print_r was removed form tcpdf_parser.php.
            - TCPDF_FILTERS class now throws an exception in case of error.
            - TCPDF_PARSER class now throws an exception in case of error unless you define the constant K_TCPDF_PARSER_THROW_EXCEPTION_ERROR to false.
            - The constant K_TCPDF_PARSER_IGNORE_DECODING_ERRORS can be set to tru eto ignore decoding errors on TCPDF_PARSER.

    6.0.027 (2013-09-14)
            - A bug in tcpdf_parser wen parsing hexadecimal strings was fixed.
            - A bug in tcpdf_parser wen looking for statxref was fixed.
            - A bug on RC4 encryption was fixed.

    6.0.026 (2013-09-14)
            - A bug in tcpdf_parser wen decoding streams was fixed.

    6.0.025 (2013-09-04)
            - A pregSplit() bug was fixed.
            - Improved content loading from URLs.
            - Improved font path loading.