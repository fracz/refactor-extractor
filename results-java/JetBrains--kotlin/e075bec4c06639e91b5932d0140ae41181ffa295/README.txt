commit e075bec4c06639e91b5932d0140ae41181ffa295
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Tue Jan 31 22:48:55 2012 +0400

    JavaDescriptorResolver refactoring

    * kill JavaDescriptorResolver.typeParameterDescriptorCache
    * use ClassOrNamespaceDescriptor instead of DeclarationDescriptor

    Code is a bit ugly now, but simpler than before.