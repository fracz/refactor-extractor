commit 3d5087f6e9efaad620e85c7409c03f248d41ca34
Author: Dmitry Jemerov <yole@jetbrains.com>
Date:   Thu Oct 13 10:34:35 2011 +0200

    spell checker refactored nafig, part 2: tokenizer receives a TokenConsumer instead of returning an array of tokens; use names validator from the right language to check if an element is a keyword instead of asking all of them