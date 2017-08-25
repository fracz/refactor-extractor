commit d5acc30de20298eb6ed7545e70484599c4d95867
Author: Anika Henke <anika@selfthinker.org>
Date:   Mon Apr 9 17:36:33 2012 +0100

    rewrote and improved HTML for TOC

    Attention: Template authors need to adjust their CSS!

    Original structure:
    div.toc >
      div#toc__header.tocheader.toctoggle > span#toc__toggle.toc_close|toc_open > span
      div#toc__inside > ul.toc > li.level1 > div.li > span.li > a.toc

    New structure:
    div#dw__toc.open|close >
      h3 > strong > span
      ul.toc > li.toc > div.li > a