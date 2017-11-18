commit a223bdd2ad5f87c8dd63793d6c803481e004ef79
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Mon Feb 9 08:36:21 2015 +0100

    BufferedCharSeeker performance improvements

    and reducing delimiter to int instead of int[] since there's currently
    no longer a need for multiple delimiters in seek.