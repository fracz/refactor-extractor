commit fc0e5767347572820e6996e1cfa3e977b6b58796
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Nov 23 19:35:10 2011 +0100

    Refactored the api around the tarTree and introduced the concept of Resource.

    Meaty check-in but I didn't figure out an easy way to increment it. Instead of ArchiveFileTree specialization of the FileTree I made the FileTree work with abstract concept of a Resource rather than a decompressor. This is a first step towards 'Resources', still long way to go, it will be improved as we start use them more broadly. The idea is a result of pairing with Adam.

     - For now, I only implemented the ReadableResource because that's the only thing I need at the moment. In future we will change the resource factory methods to return Resource instances rather than ReadableResource instances
     - Added a handful of methods to the FileOperations but I'm meaning to namespace them soon.
     - got rid of the tarTree method that accepted a closure. We don't need it any more as the FileTree produced by the tarTree is immutable
     - some refactoring and possibly some coverage still pending. Namespacing of resource factory methods pending.

     Some benefits:
     - introduced 'Resource' and made first steps towards this path
     - flexibility. the user can provide his own implementation of Resource and hence is able to work with exotic tars or (or exotic locations of tars)
     - no need to add more methods to the Project API (e.g. various overloads of tarTree not needed, tarTree is immutable)