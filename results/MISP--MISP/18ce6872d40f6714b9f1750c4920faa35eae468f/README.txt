commit 18ce6872d40f6714b9f1750c4920faa35eae468f
Author: Iglocska <andras.iklody@gmail.com>
Date:   Tue Mar 29 20:05:50 2016 +0200

    Handling of the "freetext" return format via the enrichment modules, and error handling fixed

    - freetext is now a valid return format, it will allow module developers to return an unparsed text blob which MISP will try to loop through the freetext import's detection mechanism
    - still a lot of improvements to be done for the detection mechanism

    - error handling for modules, instead of discarding errors they are now shown as a flash message on the freetext import result screen