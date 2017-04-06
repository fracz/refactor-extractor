commit 61c435e6a9b3d2888a2c8e6d65e643bcde14d280
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu Jan 21 19:35:46 2016 +0100

    PhraseSuggestionBuilde: Refactor DirectCandidateGenerator

    As a prerequisite for refactoring the whole PhraseSuggestionBuilder
    to be able to be parsed and streamed from the coordinating node, the
    DirectCandidateGenerator must implement Writeable, be able to parse
    a new instance (fromXContent()) and later when transported to the
    shard to generate a PhraseSuggestionContext.DirectCandidateGenerator.
    Also adding equals/hashCode and tests and moving DirectCandidateGenerator
    to its own DirectCandidateGeneratorBuilder class.