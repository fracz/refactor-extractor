commit 039234f8a341d0b4adf31216073c9477385676a6
Author: Ignace Nyamagana Butera <nyamsprod@gmail.com>
Date:   Fri Mar 31 13:23:57 2017 +0200

    Improve Codebase

    - addStreamFilter now supports the optional $params parameter from stream_filter_append
    - improved documentation and docblock
    - improve Converter return type hint
    - CharsetConverter::convert always returns a Iterator