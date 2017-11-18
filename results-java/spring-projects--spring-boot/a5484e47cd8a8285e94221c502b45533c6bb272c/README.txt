commit a5484e47cd8a8285e94221c502b45533c6bb272c
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Thu Aug 28 09:34:25 2014 -0700

    Extract getServletContextInitializerBeans logic

    Extract the hairy logic from getServletContextInitializerBeans() to a
    new class and refactor things a little.