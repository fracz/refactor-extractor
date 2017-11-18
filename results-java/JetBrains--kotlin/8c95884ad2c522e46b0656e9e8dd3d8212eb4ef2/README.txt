commit 8c95884ad2c522e46b0656e9e8dd3d8212eb4ef2
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Mon Oct 7 20:49:44 2013 +0400

    Add new functionality to "Change signature" refactoring

    Extract single point of entry for all change signature refactorings and fixes (remove parameter, add parameter)
    Change signature now affects overriding functions as well
    Ask the user whether he wants to refactor base function(s) or the selected one if appropiate
    Fix a problem with descriptorToDeclaration in JetChangeSignatureHandler
    Rename: JetFunctionPlatformDescriptor -> JetMethodDescriptor