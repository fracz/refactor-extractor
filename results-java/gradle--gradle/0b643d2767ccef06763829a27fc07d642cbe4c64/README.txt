commit 0b643d2767ccef06763829a27fc07d642cbe4c64
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Jan 23 13:00:37 2013 +0100

    Configure on demand. Some refactorings & reworking of the coverage.

    Now the BuildActionsFactoryTest is not influenced by gradle.properties file present in local dev environment. Moved some coverage to more relevant spot (e.g. the properties precedence order moved to GradlePropertiesConfigurerTest).