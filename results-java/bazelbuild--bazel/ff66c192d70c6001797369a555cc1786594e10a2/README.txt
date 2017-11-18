commit ff66c192d70c6001797369a555cc1786594e10a2
Author: Michael Staib <mstaib@google.com>
Date:   Thu Jan 14 22:40:37 2016 +0000

    ConfigurationFragmentPolicy: assume Skylark names, allow merging.

    In preparation for allowing aspects to have their own configuration fragments
    specified, allow ConfigurationFragmentPolicy.Builder to merge with built policies
    more easily, setting up SetMultimaps in place of maps of sets. This changes how
    named (Skylark) fragments are declared in the RuleContext builder, hopefully to
    be a bit easier to write.

    In order to do this, make SkylarkModuleNameResolver the only name resolver in use
    (because it is the only name resolver which exists) so as to not worry about
    collisions of different name resolvers.

    This also changes isLegalConfigurationFragment's one-argument form to mean
    "legal in ANY configuration" rather than "legal in the target (NONE)
    configuration", as that is how it's used by TransitiveTargetFunction. Uses of it to
    mean the latter have been revised to be more explicit.

    Also in this CL:
    * refactor ConfigurationFragmentPolicy to enforce its contracts about which
    ConfigurationTransitions are legal
    * use containsEntry or containsValue rather than looking in get(key) or values()
    for the configuration fragment multimaps
    * add tests for ConfigurationFragmentPolicy
    * make SkylarkModuleNameResolver a static method

    --
    MOS_MIGRATED_REVID=112191439