commit 25c43e7bfc64714aef5a50ab4ac1eeed7cc3ea9a
Author: Zalim Bashorov <Zalim.Bashorov@jetbrains.com>
Date:   Thu Dec 12 17:09:17 2013 +0400

    JS backend: refactoring:
        - make static LiteralFunctionTranslator#translate;
        - use TranslationContext#define instead  of  direct use DefinitionPlace.