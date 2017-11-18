commit 0205cd51d0601666e23acc3833ace828d5f744a7
Author: Daniil Ovchinnikov <daniil.ovchinnikov@jetbrains.com>
Date:   Wed May 10 18:23:37 2017 +0300

    [groovy] console: show module popup under the action button
    - extract GroovySelectModuleStep
    - add ability to create a popup but not show it
    - refactor ModuleChooserUtil and GroovyConsoleUtil
    - move strings to bundle