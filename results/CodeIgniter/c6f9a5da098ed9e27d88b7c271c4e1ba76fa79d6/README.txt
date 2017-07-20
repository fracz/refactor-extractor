commit c6f9a5da098ed9e27d88b7c271c4e1ba76fa79d6
Author: lysenkobv <lysenkobv@gmail.com>
Date:   Wed Oct 10 20:11:34 2012 +0300

    libraries/Encrypt.php decode improvement

    if base64 string is NO valid the result of decoded string is something like this "23Y�����������S�������i��!q"
    (base64_encode(base64_decode($string)) !== $string) check is this base64 string valid