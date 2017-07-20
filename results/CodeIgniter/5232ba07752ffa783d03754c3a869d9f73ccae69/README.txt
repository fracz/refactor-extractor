commit 5232ba07752ffa783d03754c3a869d9f73ccae69
Author: Andrey Andreev <narf@bofh.bg>
Date:   Sat Oct 27 15:25:05 2012 +0300

    Docblock improvements to the Config library and remove CI_Config::_assign_to_config()

    Existance of _assign_to_config() is pointless as this method
    consists just of a foreach calling CI_Config::set_item() and
    is only called by CodeIgniter.php - moved that foreach() in
    there instead.