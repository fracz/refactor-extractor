commit 65ea9dddea74a69b6fc02e53b0977b696c266f17
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Fri Aug 24 18:12:50 2012 +0400

    KotlinToJavaTypesMap refactoring

    removed repeating type names from 'isForceReal'
    removed class SpecialTypeKey (added map for nullable types)
    moved mapping Any-Object from JetTypeMapper