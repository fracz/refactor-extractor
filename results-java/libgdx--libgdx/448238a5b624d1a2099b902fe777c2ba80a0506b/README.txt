commit 448238a5b624d1a2099b902fe777c2ba80a0506b
Author: badlogicgames <badlogicgames@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Mon Nov 21 11:13:29 2011 +0000

    [changed] improved AssetLoadingTask behavior, uses 2 ticks for assets that don't have a dependency. Minimum asset loading time is thus the same as 2 / FPS. At 60fps that's 32ms, 0.032 seconds. Hrm.