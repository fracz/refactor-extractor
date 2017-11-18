commit fa05ba0b0d39fae1d2cb3d98fbee0aef6a9fed88
Author: Siyamed Sinir <siyamed@google.com>
Date:   Tue Jan 12 10:54:43 2016 -0800

    Sort the result of SpannableStringBuilder.getSpans

    SpannableStringBuilder used to return the result of getSpans in the
    order of start indices because of the interval tree it uses. However,
    style spans has to be applied in the order of insertion to get
    predictable results. Sorted the results of getSpans ordered first by
    priority flag and then by insertion time. Moreover improved the
    performance of SpannableStringInternal copy constructor.

    Bug: 26240132
    Change-Id: I0b0fa7bb30a3bd9ca37dedca66d8993718586027