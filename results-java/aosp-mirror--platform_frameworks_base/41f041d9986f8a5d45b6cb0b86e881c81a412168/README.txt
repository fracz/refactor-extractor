commit 41f041d9986f8a5d45b6cb0b86e881c81a412168
Author: Chet Haase <chet@google.com>
Date:   Tue Oct 12 16:29:28 2010 -0700

    Remove generics from Animator APIs

    Change the manner of constructing Animator-related objects from constructors
    via generics to factory methods with type-specific method names. Should
    improve the proliferation of warnings due to generics issues and make the
    code more readable (less irrelevant angle brackets Floating around).

    Change-Id: I7c1776b15f3c9f245c09fb7de6dc005fdba58fe2