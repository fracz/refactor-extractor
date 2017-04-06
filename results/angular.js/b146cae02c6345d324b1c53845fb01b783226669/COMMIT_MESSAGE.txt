commit b146cae02c6345d324b1c53845fb01b783226669
Author: Leonardo Braga <leonardo.braga@gmail.com>
Date:   Mon Dec 29 20:40:59 2014 -0500

    refactor(minErr): cleanup the generation of the error message

    Removes a "magic number" used multiple times in the code
    Removes unnecessary variables "arg" and "prefix"
    Removed a condition within the "for" loop that generates query string parameters