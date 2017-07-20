commit 4866ce3cbb2fdd50a997b3dcb75448d455ecd7f0
Author: savvot <savvot@gmail.com>
Date:   Thu Jun 5 21:01:15 2014 +0200

    yii\redis\ActiveRecord::deleteAll() refactoring - no need to write to socket 2 times when nothing found