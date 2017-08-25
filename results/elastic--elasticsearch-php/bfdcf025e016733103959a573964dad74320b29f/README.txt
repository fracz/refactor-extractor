commit bfdcf025e016733103959a573964dad74320b29f
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Thu Sep 19 10:54:11 2013 -0400

    Initial refactoring of ConnectionPools

    -Adds abstract base class for connection pools
    -Adds StaticConnectionPool
    -Modifications to AbstractConnection to support states
    -Test for StaticConnectionPool