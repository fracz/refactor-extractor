commit 2ba8cdfa4230defb531fb146e6c8e97f894038a8
Author: Artem Zatsarynnyi <azatsarynnyy@codenvy.com>
Date:   Tue Oct 11 11:35:42 2016 +0300

    Client command framework improvements (#2730)

    Client command framework improvements

    1. Introduced contextual commands.
    2. Added ${explorer.current.file.parent.path} macro.
    3. Cleaned up unused resources.
    4. Refactored code in order to simplify it and made it more clear and understandable:

    - simplified a way of providing new command types;
    - command types api moved to ide-api to avoid unnecessary dependency on machine extension;
    - command management extracted from EditCommandsPresenter to the separate facade - CommandManager. So EditCommandsPresenter now doesn't perform several http requests manipulating with commands but only one;
    - refactored code related to the obsolete terminology CommandPropertyValueProvider -> Macro;
    - macro can provide its description (it will be really useful for showing it in UI);
    - macros processing separated from the command execution and extracted to the separate component MacroProcessor since it's used not only for the commands but in debug configurations for example.