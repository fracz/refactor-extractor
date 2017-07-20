commit 4e1887a721df57c50bf7c5640d3baafa07d33373
Author: rubenrua <rubenrua@gmail.com>
Date:   Thu Feb 16 22:00:57 2017 +0000

    Improve memory usage resolving dependencies

    It is known that composer update takes a lot of memory: #5915, #5902,

    I am playing with a profiler (@blackfireio) to make a demo in my local
    PHP meetup (@phpvigo) and I found out a way to use less memory. These
    are my first tests:

    * Private project using PHP 5.6:
      * Memory: from 1.31GB to 1.07GB
      * Wall Time: from 2min 8s to 1min 33s

    * symfony-demo using PHP 7.1 in my old mac book:
      * Memory: from 667MB to 523MB
      * Wall Time: from  5min 29s to 5min 28s

    Not use an array inside conflict rules is this improvement main idea:

    ```php
    <?php
    //Memory 38MB
    gc_collect_cycles();
    gc_disable();

    class Rule
    {
        public $literals;

        public function __construct(array $literals)
        {
            $this->literals = $literals;
        }
    }

    $rules = array();

    $i = 0;
    while ($i<80000){ //
        $i++;

        $array = array(-$i, $i);
        $rule = new Rule($array);
        $rules[] = $rule;
    }
    ```

    ```php
    <?php
    //Memory 11.1MB
    gc_collect_cycles();
    gc_disable();

    class Rule2Literals
    {
        public $literal1;
        public $literal2;

        public function __construct($literal1, $literal2)
        {
            $this->literal1 = $literal1;
            $this->literal2 = $literal2;
        }
    }

    $rules = array();

    $i = 0;
    while ($i<80000){ //
        $i++;

        $rule = new ConflictRule(-$i, $i);
        $rules[] = $rule;
    }
    ```

    More info https://github.com/composer/composer/pull/6168