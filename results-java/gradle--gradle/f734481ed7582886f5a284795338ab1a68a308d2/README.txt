commit f734481ed7582886f5a284795338ab1a68a308d2
Author: Adam Murdoch <adam@gradle.com>
Date:   Thu Apr 28 10:24:00 2016 +1000

    Improve property and method lookup from some configuration closures, by short-circuiting the inefficient lookup in `Closure` and reusing the lookup in `ConfigureDelegate` instead.

    Currently this improvement is applied only in a few places in the DSL, in particular when configuring any named container (but not its elements). A later change will roll this out more widely.

    This change also removed duplicate property and method lookup on the owner object for these closures, improves error reporting on missing property or method so that the actual target of the configure closure is reported, and also cleans up the closure parameter and closure.delegate objects that are visible to the closure body so that they are always the target object.