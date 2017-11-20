commit 6254a1bccd22a57de7eede67abd8adb333c75cb1
Author: Havoc Pennington <hp@pobox.com>
Date:   Mon Dec 5 14:25:22 2011 -0500

    improve error messages for reserved chars

    Requires passing the char through the tokenizer to the parser
    since the parser will have more contextual information to
    include in the error.