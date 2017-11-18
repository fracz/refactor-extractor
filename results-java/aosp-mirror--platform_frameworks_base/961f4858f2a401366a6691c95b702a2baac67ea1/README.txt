commit 961f4858f2a401366a6691c95b702a2baac67ea1
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Mon Feb 1 20:25:54 2016 -0800

    Window resize reporting improvement when docked stack is created

    There were a few issues with window resize reporting when a docked
    stack is created that caused the client to get frames for fulscreen
    even though the task was in split-screen mode.

    - Ignore docked stack visibility when get bounds for other stack when
    resizing due to docked stack.
    - Immediately resize all other stacks in activity manager when the
    docked stack is attached to the display.
    - Defer surface layouts for the uncheck portion of moving a task to a
    stack.
    - Don't perform layout of all windows on a display when a stack is
    attached to the display since it doesn't have any windows yet.

    Bug: 26861802
    Change-Id: I2c7f31153da48618e90607c98ec5b29492b6ef38