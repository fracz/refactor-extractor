commit 36c8b431f8724e20e42d903e94fa979acf85e9c7
Author: lvca <l.garulli@gmail.com>
Date:   Tue May 6 10:11:15 2014 +0200

    Fixed issue #2313 about Parallel Query

    I’ve also improved record parser to be much faster. With many fields
    the measured improved is 6x with no parallel and 9x with parallel query!