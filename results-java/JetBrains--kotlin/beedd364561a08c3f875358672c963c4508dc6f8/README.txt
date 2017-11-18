commit beedd364561a08c3f875358672c963c4508dc6f8
Author: James Strachan <james.strachan@gmail.com>
Date:   Thu Mar 29 16:35:58 2012 +0100

    refactored out the KDocLoader stuff from the KotlinCompiler; also introduced CompilerPluginContext to wrap up the Project, BindingContext and List<JetFiles> in case a plugin were to need any of those things (or we added extra stuff later like the environment)