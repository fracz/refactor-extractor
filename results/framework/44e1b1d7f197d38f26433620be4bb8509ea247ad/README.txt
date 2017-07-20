commit 44e1b1d7f197d38f26433620be4bb8509ea247ad
Author: Dan Jordan <dan@dan-jordan.co.uk>
Date:   Fri Aug 12 15:16:43 2016 +0100

    [5.3] Turn on pretty printing of JSON when test is unable to find fragment (#14779)

    * Turn on pretty printing of JSON when test is unable to find fragment

    * Add line breaks to improve output

    * Use PHP_EOL instead

    * Use string concatenation operator instead of constant interpolation