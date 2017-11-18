commit 77d6bb4f065b14465418a80fc39fbe64d9162af1
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Thu Sep 27 00:36:25 2012 +0200

    removed nullary test constructor of ScalaCompile because it would lead to problems with real usage of the task
    instead, improved AbstractTaskTest to handle injection of a task's constructor arguments

    - AbstractTaskTest now has a service registry; tests can use it to register services that need to be injected into the task's constructor
    - on the way, fixed some incorrect usages of @Before