commit 22c65a30ecce6fa3a8e9b925033136a7b9d6482e
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Nov 8 19:27:04 2011 +0100

    [parsing notations] Some rename jobs, segregated NotationParser interfaces so that I don't have to implement some methods I don't need in some implementators. Added a marker interface for better code navigation/readability.