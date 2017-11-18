commit f7cfe8e24903da778a4388c8ba5cbdd8137aa5ee
Author: Emil Sjolander <emilsj@fb.com>
Date:   Wed Mar 29 12:49:15 2017 -0700

    Keep around delegate component in debug mode

    Summary: While in debug mode keep track of all the delegate components responsible for creating an InternalNode. This makes things easier to debug and know the flow of data. This could also greatly improve stetho integration.

    Reviewed By: IanChilds

    Differential Revision: D4721424

    fbshipit-source-id: 1832092ae65015107bbb257b24d7b5fdadff89aa