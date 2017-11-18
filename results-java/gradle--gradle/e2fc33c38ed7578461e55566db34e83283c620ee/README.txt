commit e2fc33c38ed7578461e55566db34e83283c620ee
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Jul 30 14:32:18 2014 +0200

    GRADLE-3099 Improved the multiline javadoc options for linux/mac. I was unable to make it working for windows. As of now, the behavior is improved on linux/max: line breaks will be handled automatically. On windows, the behavior is exactly the same, javadoc will barf with the same error.