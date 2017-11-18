commit 5713651230f2ec6502db45010e942ab866b14a93
Author: Dennis Oelkers <dennis@torch.sh>
Date:   Thu Mar 20 14:03:35 2014 +0100

    Mega refactoring of the persistence layer.

    Extracting all persistence logic into separate DAO service classes leaving only value classes.
    Extracting PersistedService base class and interface.
    Changing all static methods to instance methods.
    Removing core object reference in service classes and user MongoConnection directly.
    Extracing interfaces for models and service classes.
    Changing references of model implementation classes to references of model interfaces.
    Adding CollectionName annotation to replace static field.
    Adding bindings for service classes in Jersey binder.

    TODO:
    Dashboard widgets are broken.
    AlertCondition tests need a major rewrite.