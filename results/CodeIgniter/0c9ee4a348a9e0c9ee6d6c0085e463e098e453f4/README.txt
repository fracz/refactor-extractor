commit 0c9ee4a348a9e0c9ee6d6c0085e463e098e453f4
Author: Greg Aker <greg.aker@ellislab.com>
Date:   Wed Apr 20 09:40:17 2011 -0500

    Refactoring the loader to set protected class variables.

    Moved _ci_autoload(), which is used in CI_Controller to be a public method.  Also added CI_Loader::set_base_classes() to be called in the controller so we're not setting protected vars in another class.

    Also refactored in the form_helper so it's not trying to access protected vars in CI_Loader.  Added the is_loaded() method to the loader to take care of the checks that were being done there.