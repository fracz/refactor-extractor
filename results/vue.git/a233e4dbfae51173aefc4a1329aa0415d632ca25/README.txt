commit a233e4dbfae51173aefc4a1329aa0415d632ca25
Author: Blake Newman <blake.newman@sainsburys.co.uk>
Date:   Tue May 3 21:42:14 2016 +0100

    improved SSR rendering

    - Reduced duplicated code
    - Added more test coverage
    - Added syncronous component resolving support
    - Fixed component lifecycle
    - Added raw render method, to allow custom streaming possibilities