commit e2c414d77dcd456b466fdfd0a469c29e321a2a61
Author: ctiao <calmer91@gmail.com>
Date:   Wed Dec 9 11:42:45 2015 +0800

    use SystemClock.elapsedRealtime() instead of SystemClock.uptimeMills()
    wrap SystemClock into *.danmaku.util ,in order to facilitate refactor