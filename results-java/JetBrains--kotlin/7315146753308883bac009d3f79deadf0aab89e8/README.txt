commit 7315146753308883bac009d3f79deadf0aab89e8
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Oct 14 14:23:41 2014 +0400

    Fix and refactor ExpressionCodegen#pushClosureOnStack

    - change Closure parameter to ClassDescriptor, load closure in the beginning
    - invert and rename boolean parameter, also use it only to prevent pushing
      dispatch receiver (not the extension receiver) onto the stack because the
      only non-trivial usage of it is preceded by manual handling of the dispatch
      (not extension) receiver. This fixes innerOfLocalCaptureExtensionReceiver.kt