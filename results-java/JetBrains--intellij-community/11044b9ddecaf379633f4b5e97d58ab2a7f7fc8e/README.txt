commit 11044b9ddecaf379633f4b5e97d58ab2a7f7fc8e
Author: nik <Nikolay.Chashnikov@jetbrains.com>
Date:   Thu Nov 2 16:26:10 2017 +0300

    rename refactoring: fix selecting rename handler via keyboard

    'ActionListener' isn't called in some cases, e.g. if an item is selected by pressing 'right arrow' button (IDEA-180637).