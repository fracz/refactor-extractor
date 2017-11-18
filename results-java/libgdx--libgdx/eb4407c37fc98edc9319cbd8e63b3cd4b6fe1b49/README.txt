commit eb4407c37fc98edc9319cbd8e63b3cd4b6fe1b49
Author: Daniel Heinrich <dannynullzwo@gmail.com>
Date:   Fri Sep 14 17:05:09 2012 +0800

    first little refactoring
    - moved particle stuff in own package
    - instead of subclassing ParticleEmitter use factorys
    - use BitSet instead of boolean[] for active Particles