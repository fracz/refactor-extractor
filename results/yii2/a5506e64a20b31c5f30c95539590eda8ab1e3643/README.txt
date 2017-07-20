commit a5506e64a20b31c5f30c95539590eda8ab1e3643
Author: savvot <savvot@gmail.com>
Date:   Thu Jun 5 21:01:15 2014 +0200

    yii\redis\ActiveRecord::deleteAll() refactoring - no need to write to socket 2 times when nothing found