commit 33e43ac54597621c97ff1b7f65b01ded339b486e
Author: Jacksgong <igzhenjie@gmail.com>
Date:   Sat Apr 9 01:20:35 2016 +0800

    refactor(double-check-callback): add keep flow and keep ahead for status update, guarantee the status callback in 'restart from low memory' or 'filedownloader process killed and restarted' can be handled more robust