commit 62d2ead8ffafa84f1f5aaef0f8fa1f0314550b8d
Author: Mark Clark <mr.guessed@gmail.com>
Date:   Thu Sep 25 18:37:03 2014 -0700

    Standardize the MAP/Transform file naming convention for all examples:
        *In.map - for in: parameter usage
        *Out.map - for out: parameter usage
        *Command.map - for command: parameter usage

    Add extensive JavaDoc to the *BindingConfig classes.

    Rename a few methods to make their purpose better.
    "private"(ize) a few methods, since they didn't need to be part of the public JavaDoc

    Change the implementation of transformCommand() to return null when a Command Transform hasn't been specified in the binding, to simplify the downstream logic

    Move more logic into the MiosBindingConfig object, to improve the overall encapsulation, and avoid/limit the number of "conversions" going on.

    Rename class MiosConnector to MiosUnitConnector to better relate to it's intended purpose.

    Change the MiosConnector code, so it "unconditionally" attempts to use transformCommand(), and looks for a null response to indicate that it doesn't handle commands (via null return values)

    Convert more logic to using String.format() instead of string-glue to ease the migration to i18n ResourceBundles down the line.

    Eliminate getItemType from MiosBindingProvider/Impl since it was never used ;)