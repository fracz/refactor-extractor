commit 0d8d831b7c0bb65d3e15aee4ac32280a4de2a824
Author: Alan Jenkins <alan.christopher.jenkins@gmail.com>
Date:   Fri Nov 23 11:11:14 2012 +0000

    Add post_key_exists(), use it to improve code consistency

    This avoids the uncommented use of isset() and $_POST, particularly
    in the output of the module builder.