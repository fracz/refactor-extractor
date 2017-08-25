commit 4e62faea8bcc6a3fbd8d0b315cf079d013d895bc
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Thu May 4 05:23:46 2017 +0200

    Up PHPCS minimum requirement to take advantage of improved token arrays.

    PHPCS 2.9.0 contains a new `PHP_CodeSniffer_Tokens::$textStringTokens` array as well as several other improvements to some of the predefined arrays in `PHP_CodeSniffer_Tokens` which are being used by WPCS.

    It therefore makes sense to take advantage of these upstream improvements to automatically improve the results of our sniffing.

    In two places, replacing an existing manually created array with the new `PHP_CodeSniffer_Tokens::$textStringTokens` array means that the `T_INLINE_HTML` token is added to the array.
    For both instances, the code has been reviewed and it was determined that the `T_INLINE_HTML` token can never be encountered there anyway as both examine function calls, so this should not cause any issues.

    Includes some extra unit tests.

    Note: in some unit tests the `??=` syntax is being used. This is a new syntax which is expected to be introduced in PHP 7.2.
    The RFC for this has been approved, though the actual change has not been merged yet into PHP itself. All the same, PHPCS already accounts for it, so we may as well test that it does so correctly.