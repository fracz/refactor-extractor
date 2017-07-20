commit 905f4ec0f835dc9eb0a34f37c297bd6a02d519ba
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Wed Jul 13 18:04:28 2016 +0000

    Mail: Improve handling of UTF-8 address headers.

    Previously, `wp_mail()` implemented Reply-To as a generic header, using
    PHPMailer's `addCustomHeader()`. As such, the email address portion of
    the header was being incorrectly encoded when the name portion
    contained UTF-8 characters. Switching to PHPMailer's more specific
    `addReplyTo()` method fixes the issue.

    For greater readability, the handling of all address-related headers
    (To, CC, BCC, Reply-To) has been standardized.

    Props szepe.viktor, iandunn, bpetty, stephenharris.
    Fixes #21659.
    Built from https://develop.svn.wordpress.org/trunk@38058


    git-svn-id: http://core.svn.wordpress.org/trunk@37999 1a063a9b-81f0-0310-95a4-ce76da25c4cd