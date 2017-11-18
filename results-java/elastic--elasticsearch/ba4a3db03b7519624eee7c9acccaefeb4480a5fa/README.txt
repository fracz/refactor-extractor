commit ba4a3db03b7519624eee7c9acccaefeb4480a5fa
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Oct 11 21:11:58 2016 +0200

    Use ConstructingObjectParser for parsing DirectCandidateGenerator

    When refactoring DirectCandidateGeneratorBuilder recently, the
    ConstructingObjectParser that we have today was not available. Instead we used
    some workaround, but it is better to remove this now and use
    ConstructingObjectParser instead.