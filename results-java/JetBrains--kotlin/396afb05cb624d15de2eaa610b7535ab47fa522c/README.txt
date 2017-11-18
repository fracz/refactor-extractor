commit 396afb05cb624d15de2eaa610b7535ab47fa522c
Author: Zalim Bashorov <Zalim.Bashorov@jetbrains.com>
Date:   Thu Feb 13 14:20:42 2014 +0400

    JS backend: minor refactorings
      - dropped useless constructor and factory method from ClassTranslator.
      - moved method which translate an object expression inside a class code to ClassTranslator.
      - LiteralFunctionTranslator#getSuggestedName -> TranslationUtils#getSuggestedName