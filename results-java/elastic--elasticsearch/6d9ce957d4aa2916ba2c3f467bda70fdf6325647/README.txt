commit 6d9ce957d4aa2916ba2c3f467bda70fdf6325647
Author: Nik Everett <nik9000@gmail.com>
Date:   Tue May 30 10:39:22 2017 -0400

    Drop name from TokenizerFactory (#24869)

    Drops `TokenizerFactory#name`, replacing it with
    `CustomAnalyzer#getTokenizerName` which is much better targeted at
    its single use case inside the analysis API.

    Drops a test that I would have had to refactor which is duplicated by
    `AnalysisModuleTests`.

    To keep this change from blowing up in size I've left two mostly
    mechanical changes to be done in followups:
    1. `TokenizerFactory` can now be entirely dropped and replaced with
    `Supplier<Tokenizer>`.
    2. `AbstractTokenizerFactory`'s ctor still takes a `String` parameter
    where the name once was.