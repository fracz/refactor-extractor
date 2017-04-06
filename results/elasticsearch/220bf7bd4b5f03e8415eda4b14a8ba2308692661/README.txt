commit 220bf7bd4b5f03e8415eda4b14a8ba2308692661
Merge: 856883f 61c435e
Author: Christoph Büscher <christoph@elastic.co>
Date:   Mon Feb 1 13:17:54 2016 +0100

    Merge pull request #16185 from cbuescher/ref-DirectCandidateGenerator

    As a prerequisite for refactoring the whole PhraseSuggestionBuilder
    to be able to be parsed and streamed from the coordinating node, the
    DirectCandidateGenerator must implement Writeable, be able to parse
    a new instance (fromXContent()) and later when transported to the
    shard to generate a PhraseSuggestionContext.DirectCandidateGenerator.
    Also adding equals/hashCode and tests and moving DirectCandidateGenerator
    to its own DirectCandidateGeneratorBuilder class.