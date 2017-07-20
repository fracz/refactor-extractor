commit 83527e85ca5dea8a66db4f8f256abc38bf150229
Author: Carsten Brandt <mail@cebe.cc>
Date:   Thu Nov 28 01:00:05 2013 +0100

    made Model::attributes() non static again

    - allows to have dynamic definition of attributes depended on the
      instance
    - there was no real need for it to be static. Places that used it static
      have been refactored.

    fixes #1318