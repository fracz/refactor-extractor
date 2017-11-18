commit cf66f95e1b6cb2980bdd1c455786f1d38476c1aa
Author: Bernd Ahlers <bernd@torch.sh>
Date:   Fri Oct 10 15:43:16 2014 +0200

    Make sure output plugin configuration gets stored with correct types.

    The plugin configuration should also be parsed with Jackson instead of
    using Map<String, Object> to avoid such hackery. This would require that
    the plugins ship POJOs for their config values though and that's a
    bigger refactoring with breaking changes...

    Fixes #733