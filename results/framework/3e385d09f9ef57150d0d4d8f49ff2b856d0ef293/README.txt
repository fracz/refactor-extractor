commit 3e385d09f9ef57150d0d4d8f49ff2b856d0ef293
Author: Malcolm Hall <malhal@users.noreply.github.com>
Date:   Sun Aug 21 23:13:05 2016 +0100

    Throw ModelNotFoundException when model bind not found

    This is a PR for Issue #13455 to bring consistency to model bind not found and findOrFail.

    The NotFoundHttpException was changed to a ModelNotFoundException and has the model set, the exact same as in Eloquent/Builder findOrFail.

    Although the Exceptions/Handler.php converts this back to a NotFoundHttpException in the prepareException method anyway (so thus should have same behaviour for projects using the default handling), by throwing a ModelNotFoundException in the first instance has the added benefit that a custom Handler subclass can render both cases of a model binding not found and a findOrFail in the same way. Furthermore, by having an exception that has the model name set on it, exception handling can be even more improved for binding failures, e.g. by default there is now the message "No query results for model [App\ModeClass]." instead of no message. This message is exactly the same as the exception from findOrFail since it is created inside the ModelNotFoundException itself.

    Before this fix, developers had to workaround this issue by providing a closure to return the correct exception on all their route models via a closure, although in the example given in this forum post, the developer didn't set the model on the exception:
    https://laracasts.com/discuss/channels/general-discussion/throw-modelnotfoundexception-using-route-model-binding