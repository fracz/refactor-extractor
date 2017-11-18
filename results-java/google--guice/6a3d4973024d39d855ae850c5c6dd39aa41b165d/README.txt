commit 6a3d4973024d39d855ae850c5c6dd39aa41b165d
Author: lukes <lukes@google.com>
Date:   Tue Aug 2 14:33:04 2016 -0700

    Extract the main bindingimpl/factory implementation of ProviderMethod into an
    abstract base class.

    This is a prefactoring for moving multibinders into the core.  All the
    multibinder factories will share a lot in common with ProviderMethods
    * They are ProviderInstanceBindings
    * The providers implement ProviderWithExtensions
    * The 'userSuppliedProvider' is a bit of a misnomer, so we provide a standard
      implementation

    This also removes the ProviderLookups from every ProviderMethod, which were
    pretty much unneeded.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=129142220