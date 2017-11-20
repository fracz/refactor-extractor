commit d3a7008ab323769ccd9b3e94436784dc4225e289
Author: cushon <cushon@google.com>
Date:   Wed Mar 22 14:47:14 2017 -0700

    Recognize safe try/finally/close patterns in MustBeClosedChecker

    Previously only try-with-resources was supported. Also extract
    the improved logic to an abstract class, and share it with
    FilesLinesLeak.

    Fixes #489

    RELNOTES: Recognize safe try/finally/close patterns in MustBeClosedChecker

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=150930609