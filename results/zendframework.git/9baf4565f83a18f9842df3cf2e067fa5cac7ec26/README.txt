commit 9baf4565f83a18f9842df3cf2e067fa5cac7ec26
Author: Dominic Luechinger <doldev@snowgarden.ch>
Date:   Tue Oct 18 22:45:31 2011 +0200

    Minor improvement: Replaced 'CURLOPT_PUT' with recommended replacement 'CURLOPT_UPLOAD'

    The cUrl documentation (http://curl.haxx.se/libcurl/c/curl_easy_setopt.html#CURLOPTPUT)
    about the 'CURLOPT_PUT' option suggest that the option is deprecated. Instead it's
    recommended to use 'CURLOPT_UPLOAD' instead.