commit 4420ae875de711a91dc10f7f4dd5a9cc62221ac8
Author: Alan Viverette <alanv@google.com>
Date:   Mon Nov 16 16:10:56 2015 -0500

    Clean up TimePicker

    No functional changes, only refactoring:
    - shorten method and variable names
    - remove unused validation callback
    - avoid using return in setters

    Change-Id: Ie7c19cfe3c5cb515695f943c534899d37ad032bb