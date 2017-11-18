commit 72852b6950020a1aab0e87f21707948501f95df4
Author: Artem Zatsarynnyi <azatsarynnyy@codenvy.com>
Date:   Mon Apr 10 15:18:17 2017 +0300

    Intelligent Commands bug fixes and code refactoring (#4745)

    * fix resetting a default command after any command modifications;
    * fix tooltip for Execute Command button in case more than one machine is launching in the workspace;
    * fix NPE on opening Commands Palette;
    * add events for listening to commands modifications: CommandAddedEvent, CommandRemovedEvent, CommandUpdatedEvent, CommandsLoadedEvent;
    minor code refactoring.