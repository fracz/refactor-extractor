commit fa1676460abf400f8533b81735de0d04f3befd1a
Author: Mark Fisher <markfisher@vmware.com>
Date:   Mon Jul 27 14:39:20 2009 +0000

    Replaced BinderSupport with a refactored AbstractBinder that delegates to a FieldBinder whose creation is the responsibility of each subclass.