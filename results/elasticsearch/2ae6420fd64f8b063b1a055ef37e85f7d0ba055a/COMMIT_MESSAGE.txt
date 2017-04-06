commit 2ae6420fd64f8b063b1a055ef37e85f7d0ba055a
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu Jan 28 20:04:15 2016 +0100

    Suggest: Add parsing from xContent to PhraseSuggestionBuilder

    For the ongoing search refactoring (#10217) the PhraseSuggestionBuilder
    gets a way of parsing from xContent that will eventually replace the
    current SuggestParseElement. This PR adds the fromXContent method
    to the PhraseSuggestionBuilder and also adds parsing code for the
    common suggestion parameters to SuggestionBuilder.

    Also adding links from the Suggester implementations registeres in the
    Suggesters registry to the corresponding prototype that is going to
    be used for parsing once the refactoring is done and we switch from
    parsing on shard to parsing on coordinating node.