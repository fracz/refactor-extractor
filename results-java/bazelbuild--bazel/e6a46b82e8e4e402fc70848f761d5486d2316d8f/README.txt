commit e6a46b82e8e4e402fc70848f761d5486d2316d8f
Author: Francois-Rene Rideau <tunes@google.com>
Date:   Fri Apr 10 18:31:43 2015 +0000

    Allow evaluation from String

    Lift the Evaluation code from the test files AbstractParserTestCase and
    AbstractEvaluationTestCase into new files EvaluationContext.
    Remove this code's dependency on FsApparatus (and thus to InMemoryFS),
    by making the Lexer accept null as filename.
    Also remove dependency on EventCollectionApparatus;
    parameterized by an EventHandler.

    Have the SkylarkSignatureProcessor use this Evaluation for defaultValue-s.

    While refactoring evaluation, have SkylarkShell use it,
    which fixes its ValidationEnvironment issues.

    TODO: refactor the tests to use this new infrastructure.

    --
    MOS_MIGRATED_REVID=90824736