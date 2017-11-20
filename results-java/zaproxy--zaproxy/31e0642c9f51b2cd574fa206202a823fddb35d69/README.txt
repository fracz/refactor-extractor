commit 31e0642c9f51b2cd574fa206202a823fddb35d69
Author: thc202 <thc202@gmail.com>
Date:   Thu Nov 1 22:34:07 2012 +0000

    Issue 391: ZAP Performance improvements

    Minimised the loadings of the HttpMessage from the database when the HttpMessage is successively used.