commit c33567a2c7283cf1320856ad1de2c7372b60be3c
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Mon Jan 27 07:21:36 2014 -0800

    Fix #982 Rename MY_Security to BF_Security

    Also:
    - added override of csrf_show_error() to improve the error message
    - cleaned up the csrf_verify() method to return $this like the original
    csrf_verify() method and streamlined the code slightly