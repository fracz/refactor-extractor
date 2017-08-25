commit 91b05b6bf9cb9fd3aeced777159422f4e8114a9c
Author: Anika Henke <anika@selfthinker.org>
Date:   Mon Apr 9 19:06:15 2012 +0100

    improved HTML for search results

    Attention: Template authors need to adjust their CSS!

    Original structure:
    div.search_result >
      a.wikilink1 > span.search_cnt
      br
      div.search_snippet

    New structure:
    dl.search_results >
      dt > a.wikilink1
      dd