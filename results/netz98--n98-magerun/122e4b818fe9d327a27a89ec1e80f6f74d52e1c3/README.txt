commit 122e4b818fe9d327a27a89ec1e80f6f74d52e1c3
Author: Alexander Menk <a.menk@imi.de>
Date:   Tue Apr 28 10:52:26 2015 +0200

    #532 Optimization fixes and improvements

    - Optimized dump was ignored, instead the original was imported
    - dump was broken if a field value contained the work "values"
    - added autocommit=0 optimization