commit 3b487a34faa3105e9aec97af52a9f0e81d9afa07
Author: tduehr <tduehr@users.noreply.github.com>
Date:   Thu Aug 3 02:19:13 2017 -0500

    Random string generation should use SecureRandom w/ Base64 encoding (#2812)

    * fix random strings

    * fix codacity issues

    * fixed various strings/lengths

    * fix checkstyle errors

    * refactor base64 a bit

    * fix checkstyle and bugs

    * use strong SecureRandom instance

    * codacity