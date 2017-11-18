commit 948827aa0667ed837c04b554fc3651afeef53eb4
Author: Zalim Bashorov <Zalim.Bashorov@jetbrains.com>
Date:   Thu Dec 12 15:45:31 2013 +0400

    JS backend: refactoring LiteralFunctionTranslator -- make possible to store translation state and create an instance for each translation instead of one instance for all translations.
    Introduced DefinitionPlace class and make creating a new definition place more safety.