commit bb2d279978d3f2877f3cc76cdff6e731b5585738
Author: Alex Pott <alex.a.pott@googlemail.com>
Date:   Thu Aug 20 09:56:42 2015 +0100

    Issue #2501319 by joelpittet, cwells, stefan.r, YesCT, nlisgo, Cottser, cmanalansan, dawehner, xjm, alexpott, josephdpurcell: Remove SafeMarkup::set() in Error::renderExceptionSafe() and _drupal_log_error() and improve backtrace formatting consistency in _drupal_log_error(), Error::renderExceptionSafe(), and DefaultExceptionSubscriber::onHtml()