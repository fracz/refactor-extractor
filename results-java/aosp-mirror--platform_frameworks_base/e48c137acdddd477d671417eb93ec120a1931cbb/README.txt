commit e48c137acdddd477d671417eb93ec120a1931cbb
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Thu Apr 9 15:55:42 2015 -0700

    Add IntentFilter auto verification - part 5

    - optimize IntentFilter verification: dont do stuff we dont want
    if we dont need to do them.

    - improve IntentFilter candidates filtering and also improve
    at the same time fix for bug #20128771: we can return the candidates
    list rigth the way if the Intent is not related to a Web data URI and
    include the "undefined verification state" ones if the first filtering
    pass does not leave any.

    Change-Id: I19f5c060f58b93530e37b4425d19ed23d2a0f4c0