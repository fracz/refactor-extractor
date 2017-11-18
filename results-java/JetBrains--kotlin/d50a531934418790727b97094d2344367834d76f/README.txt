commit d50a531934418790727b97094d2344367834d76f
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jan 13 22:14:40 2016 +0300

    Slightly improve bytecode version check during inline

    Add class name to the exception message and provide a system property to
    disable the check