commit 0b8e8efb9d117324cf9d2bfcc2fee304ada737c7
Author: paulmarshall <paulmarshall@google.com>
Date:   Wed Aug 31 10:58:38 2016 -0700

    Convert RealProviderMultimapProvider to use InternalFactory interfaces.

    This is a performance improvement CL. The goal is to avoid overhead of
    ProviderInstanceBinding + ProviderLookups, and instead use the
    InternalFactory interfaces directly. This will be one in a series of
    CLs to migrate all of RealMapBinder.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=131848624