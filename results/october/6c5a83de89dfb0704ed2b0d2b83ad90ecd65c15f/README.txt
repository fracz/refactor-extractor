commit 6c5a83de89dfb0704ed2b0d2b83ad90ecd65c15f
Author: Samuel Georges <sam@daftspunk.com>
Date:   Tue Jan 24 08:36:50 2017 +1100

    Use 127.0.0.1 in favor of localhost
    - Speed improvement
    - Consistency with redis config
    - Fixes issue with XAMPP on macOS, see: http://stackoverflow.com/questions/20723803/pdoexception-sqlstatehy000-2002-no-such-file-or-directory