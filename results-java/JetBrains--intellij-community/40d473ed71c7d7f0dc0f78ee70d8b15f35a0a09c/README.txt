commit 40d473ed71c7d7f0dc0f78ee70d8b15f35a0a09c
Author: Roman.Shein <marso.des@gmail.com>
Date:   Tue Aug 11 13:43:06 2015 +0300

    Working prototype of code style settings deriver: somewhat works for java and c++; also for scala with appropriate extension implemented.
    Has some issues with UI (FCodeStyleSettingsNameProvider is an ugly stub).
    Also formatting while running the FExtractCodeStyleAction is sometimes buggy (has significant delays or does not happen at all).
    Running time should also be improved.