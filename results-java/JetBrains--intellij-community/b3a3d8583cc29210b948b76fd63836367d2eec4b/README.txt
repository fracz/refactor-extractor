commit b3a3d8583cc29210b948b76fd63836367d2eec4b
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Wed Jan 2 15:43:12 2013 +0400

    [git] cherry-pick test refactoring:

    move creation of conflicting commits into from background into scenarios: easier to refer from these scenarios, less to execute for scenarios that don't need these commits.