commit eaa98a7dd4c180f26f073da25c68a96e93398584
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Mar 20 15:19:12 2011 +0100

    (GRADLE-1449) Improved ToolingApi so that it honors user's customizations of source folders. ToolingApi is using eclipse plugin's domain model to get the proper source folders. This way the logic that understands gradle model and can translate it to eclipse model lives in single place (e.g. eclipse plugin). Details:

    -since creation of eclipse model was getting complicated introduced a factory for that. It also improves testability
    -eclipse plugin no longer need to keep state in eclipseProjectTask field (we can get it from parent's class project field)
    -created / fixed corresponding integration tests
    -creation of source folders in ToolingApi deserved a factory so it received one. Now it's cleaner & unit testing is possible