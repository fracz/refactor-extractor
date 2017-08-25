commit 9bf634f281b0e4a99a5db96c67fab74fca2a18d1
Author: Maksim Kotlyar <kotlyar.maksim@gmail.com>
Date:   Sat Nov 23 12:23:18 2013 +0200

    [security] improve token generator.

    This is not ok. base_convert only supports up to MAX_INT-1. I recommend using base64_encode and trimming = on the right.. If the token also can be used inside the uri, then uri safe base64 encoding should be done...

    Apply comments done: https://github.com/Payum/Payum/commit/59748392d233783140547ca1240dfae61202a0c6#commitcomment-4667875