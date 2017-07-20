commit e00bebd1414f0ce911016331e8496cf65f6ffaa4
Author: Justin Hileman <justin@justinhileman.info>
Date:   Thu Feb 20 09:07:43 2014 -0800

    Override encoding to improve tokenizer performance

    Speeds up tokenizing by ~15% for most users, and by 50% for users with mbstring.func_overload set!

    See #144