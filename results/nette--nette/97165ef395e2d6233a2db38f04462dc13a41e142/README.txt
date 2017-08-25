commit 97165ef395e2d6233a2db38f04462dc13a41e142
Author: David Grudl <david@grudl.com>
Date:   Sat Oct 25 19:51:18 2008 +0000

    - Nette::Debug - Firebug support updated to new protocol (FirePHP 0.2 is required)
    - ApplicationException - fixed obscure PHP behavior
    - Nette::Loaders refactoring, removed RobotLoader::$displaceNetteLoader
    - Nette::Forms - ConventionalRenderer::renderControls accepts FormContainer as parameter
    - added Collection::append()
    - fixed some bugs