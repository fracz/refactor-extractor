commit af3c997a46ef82d43062ac255751fae6b8049be6
Author: Andrzej Fiedukowicz <Andrzej.Fiedukowicz@teradata.com>
Date:   Tue May 10 13:52:43 2016 +0200

    Move Signature creation outside of SqlFunction

    This refactor is first step to creating Signature of function ASAP
    instead of passing it as fragmented fields throughout the code.
    This will allow to simpler code flow, smaller number of argument
    and more consistent interfaces.

    Other focus point in this commit is to avoid passing Types as strings.
    The general idea is to get to the state where type will be converted
    from String to TypeSignature ASAP (while parsing annoations).
    For now it just moves String types outside of SqlScalarFunction.

    Future steps is to apply this changes to other SqlFunction based
    classes. And to remove Signature constructors accepting Strings instead
    of TypeSignature.