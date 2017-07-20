commit 7ed3013a5b9323f7d5d526aa5fe8bdaaeaad2950
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Sun Nov 11 19:07:05 2012 +0100

    switch to array_replace instead of array_merge

    we don't need the logic to merge numeric keys, as we don't have them. I could also improve the genrated code by PhpMatcherDumper a little by saving a function call.