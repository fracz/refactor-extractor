commit 7974e7eb5f3821b77691819683f9aa37f3c37473
Author: DiPeng <pengdi@go.wustl.edu>
Date:   Fri Jun 24 12:26:44 2011 -0700

    refactor($browser): hide startPoll and poll methods

    Breaks $browser.poll() method is moved inline to $browser.startpoll()
    Breaks $browser.startpoll() method is made private
    Refactor tests to reflect updated browser API

    Closes #387