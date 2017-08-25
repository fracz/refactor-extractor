commit 8e750e65e99c5c85f39c6f356b2ea9e83a473b41
Author: Ilya Goryachev <ilya.gory@yandex.ru>
Date:   Fri Jul 21 03:19:13 2017 +0300

    Automatically reconnect to the MySQL server (#654)

    * Reconnect to the MySQL server while preparing/unprepared query if the connection was lost (errno 2006)

    * limit auto reconnecting attempts

    * update stmt while autoreconnecting

    * reset auto reconnect counter; some refactoring