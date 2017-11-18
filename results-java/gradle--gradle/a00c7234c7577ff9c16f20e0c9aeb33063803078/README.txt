commit a00c7234c7577ff9c16f20e0c9aeb33063803078
Author: daz <daz@bigdaz.com>
Date:   Wed Dec 12 21:33:31 2012 -0700

    Provide a way to present user-code exceptions in build output, used for publishing withXml() methods
    - Added InvalidUserCodeException for failures in executing user-supplied code
    - Added delegating UserCodeAction which wraps any exception in InvalidUserCodeException
    - Updated DefaultExceptionAnalyser to special-case InvalidUserCodeException (this will need improvement)
    - MavenPom.withXml() and IvyModuleDescriptor.withXml() both wrap supplied actions in UserCodeAction