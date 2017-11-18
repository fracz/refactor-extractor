commit 71a1211d3dec10d4979c61e1fc370076d3b9dcf6
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Apr 17 14:09:19 2014 +0200

    Fixed GRADLE-3067. Configuration.getTaskDependencyFromProjectDependency API was not coping very well with configure-on-demand mode.

    I've fixed it in a simplest possible way - by invoking an explicit 'evaluate' on the target projects. It's not very clean and I was hesitant to do it, especially as I don't know the future of getTaskDependencyFromProjectDependency API (I would love to get rid of it). Eventually, I decided that fixing it will make Gradle better. Plus the changes has driven some cleanup in the test code and refactorings that pushed complexity out of DefaultConfiguration.

    Down the road when we work full speed at new configuration model it should be easy to spot all the places where we explicitly configure projects and deal with them in more desirable way.