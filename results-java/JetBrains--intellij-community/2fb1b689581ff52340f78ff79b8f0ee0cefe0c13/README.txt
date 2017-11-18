commit 2fb1b689581ff52340f78ff79b8f0ee0cefe0c13
Author: Dmitry Jemerov <yole@jetbrains.com>
Date:   Thu Oct 13 10:34:48 2011 +0200

    spell checker refactored nafig, part 2: tokenizer receives a TokenConsumer instead of returning an array of tokens; use names validator from the right language to check if an element is a keyword instead of asking all of them