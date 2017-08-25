commit be3d79539928f7d423d841d0b1fc49703ed9f9ad
Author: David Grudl <david@grudl.com>
Date:   Thu Jul 24 10:23:25 2008 +0000

    - Presenter::$autoCanonicalize moved to Application (as $canonicalize) and is enabled by default
    - removed Presenter::renderFinished() - use Presenter::abort() instead
    - added RedirectingException
    - redirecting now handles Application
    - added simple default error handler to Application
    - Nette::Debug - improved integration with Firebug
    - some bug fixes and improvements