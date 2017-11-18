commit eac328dc6ccb9286b1b60280e2eab6d12cc2a904
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Thu Dec 18 16:16:40 2014 +0100

    added journal command line group and initial show command

    refactored bootstrap code into a minimal injector and config base class and a "server bootstrap" one which loads the generic bindings etc for radio and server nodes
    this could be broken down further, but this should be the minimal amount needed to support simple commands