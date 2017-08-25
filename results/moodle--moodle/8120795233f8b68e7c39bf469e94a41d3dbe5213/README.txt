commit 8120795233f8b68e7c39bf469e94a41d3dbe5213
Author: Marina Glancy <marina@moodle.com>
Date:   Thu Jun 22 10:17:59 2017 +0800

    MDL-59311 course: duplicate module rebuilds cache twice

    slightly refactor duplicate_module() code so it does not rebuild course cache
    twice and also we do not need to move module if it is already the last module in a section