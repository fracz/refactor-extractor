commit a369dff2cd62a5043cc5f96523ce6478fad57e7e
Author: Samuel Audet <samuel.audet@gmail.com>
Date:   Thu May 11 11:02:58 2017 +0900

    Add BaseCudnnHelper and refactor all cuDNN-based helpers around it (#3388)

    Add property to set manually the convolution algorithms of cuDNN

    Add some documentation surrounding the AlgoMode for cuDNN