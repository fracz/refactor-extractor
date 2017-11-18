commit 1949e24ffafdc0b9248a15daf664e140c888d9a9
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu Jul 10 20:19:40 2014 +0400

    Get rid of ConstructorFrameMap, drop CodegenContext#prepareFrame()

    Also refactor FunctionGenerationStrategy to take FrameMap as an instance
    instead of creating it in a subclass