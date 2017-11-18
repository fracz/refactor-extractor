commit ef06413afce31800dc8dfee65e5f89bb610f192a
Author: Winson <winsonc@google.com>
Date:   Tue Jan 5 12:11:31 2016 -0800

    Moving more callbacks to animated events.

    - In preparation for the animation refactoring, this CL just moves the
      enter/exit callbacks that route though RecentsView into events that
      the task stack can handle directly.

    Change-Id: I90f602c5486e1781129225a73dbf97af29477479