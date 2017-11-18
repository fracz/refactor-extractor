commit b0e8e7f536e021836ec7737d6d97395f5736f2ff
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Jun 7 12:17:49 2017 -0400

    Refactor MappingContentTypeResolver implementations

    After the removal of suffix pattern matches, there is no longer a need
    to expose the list of registered file extensions.

    Also polish, refactor, and simplify the abstract base class
    AbstractMappingContentTypeResolver and its sub-classes.

    Issue: SPR-15639