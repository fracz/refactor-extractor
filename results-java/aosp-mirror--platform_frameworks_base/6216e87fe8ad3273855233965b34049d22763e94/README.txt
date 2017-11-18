commit 6216e87fe8ad3273855233965b34049d22763e94
Author: Philip Milne <pmilne@google.com>
Date:   Thu Feb 16 17:15:50 2012 -0800

    Fix remaining issue with bug #5904777

    LEFT alignment in an RTL environment had the wrong 'gravity'.

    This was due to a modelling error in GridLayout which is fixed in this CL.

    Also apply some very minor simplifications and refactorings following the
    addition of RTL support.

    Change-Id: I153bc06d3c22dcb9954e4cbdfa89625823239b89