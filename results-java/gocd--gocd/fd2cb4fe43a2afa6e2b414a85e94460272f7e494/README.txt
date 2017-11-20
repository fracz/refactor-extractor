commit fd2cb4fe43a2afa6e2b414a85e94460272f7e494
Author: Peiying Wen <wenpeiying@gmail.com>
Date:   Sun Dec 14 17:38:37 2014 -0800

    Fixed the failing unit test. Notice that I've introduced a protected
    method for better test coverage; a better way is to have a wrapper class
    around the fileUtil so we can test the shit out of every
    exception we'd like, but that'd a good follow-up refactoring commit.