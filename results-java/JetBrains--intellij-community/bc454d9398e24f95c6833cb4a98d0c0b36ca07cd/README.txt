commit bc454d9398e24f95c6833cb4a98d0c0b36ca07cd
Author: Dennis Ushakov <dennis.ushakov@gmail.com>
Date:   Tue Jul 3 12:41:54 2012 +0400

    #RUBY-11497 fixed
    #RUBY-11336 fixed
    made lexer understand keys with brackets
    improved regexp for scalar keys to handle # within
    improved comments handling to treat # without whitespace before as text