commit 1e87e2d2429c821a4c4839125f445fc86e8551a3
Author: Marko D <chivitli@managecms.com>
Date:   Wed Feb 5 17:37:37 2014 +0100

    Improvement to result display in smart search

    Results are truncated to a number of characters from the begining, so unless your query falls there, you won't see it in the results view, which may confuse the user who did the search.

    This improves the truncation so that characters around the search query are shown. If it was the last word in the article, then chosen amount of characters before it will be shown (or after if it was at the begining), otherwise it aims for the search query to be in the middle of excerpt for optimal understanding.

    If the match was not in the body of the description, then the first XXX characters will be shown like before.

    Maybe such function would be useful to add to JHtml.string?