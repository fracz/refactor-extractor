commit 9a2565dfd9e83d6cc80b9e6207d7a8a6e0cca4dc
Author: Vyacheslav Lukianov <lvo@jetbrains.com>
Date:   Tue Jul 4 16:10:34 2006 +0400

    reviewed and refactored registration of custom types in order to simplify it
    AntTypeDef uses the same scheme of clearing caches as AntMacroDef and AntPresetDef
    AntTypeDef: loading classes by classpaths splitted with the ':' (IDEADEV-6808)
    AntStructuredElement can be "typeDefined" and can contain reference onto its type definition (like macrodef and presetdef)