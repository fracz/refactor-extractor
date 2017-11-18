commit 8f826eed85cb030ba51f8c2a9d7bb8153bda1965
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon Oct 6 15:20:35 2014 +0400

    [git] Fix auto-fill of the author field on cherry-pick

    It was broken during some recent author field improvements.
    Pre-filled author should be placed to the combobox during editor
    creation.