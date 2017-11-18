commit b880d880c6cd989eacc28c365fc9a41d31900da1
Author: Jeff Brown <jeffbrown@google.com>
Date:   Mon Feb 10 19:47:07 2014 -0800

    Make SystemService constructor take a Context.

    This change simplifies the process of initializing a SystemService
    by folding the onCreate() step back into the constructor.  It removes
    some ambuiguity about what work should happen in the constructor and
    should make it possible for services to retain most of their final
    fields after refactoring into the new pattern.

    Change-Id: I25f41af0321bc01898658ab44b369f9c5d16800b