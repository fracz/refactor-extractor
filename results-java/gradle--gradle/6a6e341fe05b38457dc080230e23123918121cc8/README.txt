commit 6a6e341fe05b38457dc080230e23123918121cc8
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Jul 25 23:59:04 2014 +0200

    GRADLE-3099 Javadoc options now support multiline values.

    This is especially useful for certain html javadoc options like the 'header'. Header typically contains html: scripts or some javascript that enhances the javadoc usability. We could support multilines only for specific options, for example those that contain html content. However, supporting multilines for all options is a simple improvement and we can grow it / change it if needed.