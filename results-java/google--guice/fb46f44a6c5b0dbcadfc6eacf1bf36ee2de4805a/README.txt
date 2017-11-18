commit fb46f44a6c5b0dbcadfc6eacf1bf36ee2de4805a
Author: lukes <lukes@google.com>
Date:   Tue Aug 2 16:32:05 2016 -0700

    Automated g4 rollback of changelist 129142220.

    *** Reason for rollback ***

    breaks moduledependencytestcase tests and other weird things

    *** Original change description ***

    Extract the main bindingimpl/factory implementation of ProviderMethod into an
    abstract base class.

    This is a prefactoring for moving multibinders into the core.  All the
    multibinder factories will share a lot in common with ProviderMethods
    * They are ProviderInstanceBindings
    * The providers implement ProviderWithExtensions
    * The 'userSuppliedProvider' is a bit of a misnomer, so we provide a standard
      implementation

    This also removes the ProviderLookups from every ProviderMethod, which were
    pr...

    ***

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=129156867