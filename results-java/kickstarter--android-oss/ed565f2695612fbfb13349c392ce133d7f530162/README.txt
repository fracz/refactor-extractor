commit ed565f2695612fbfb13349c392ce133d7f530162
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Wed Jul 12 15:26:53 2017 -0400

    [Messages] More bugfixes (#129)

    * set coordinator layout ids so toolbar state is preserved on rotation LOL

    * request keyboard focus when no messages

    * Add MessageSubject class, refactor to use it

    * Refactor MessagesVM with MessageSubject logic

    * Use user ids

    * Fix up logic for showing keyboard when no messages

    * clean up messagebody