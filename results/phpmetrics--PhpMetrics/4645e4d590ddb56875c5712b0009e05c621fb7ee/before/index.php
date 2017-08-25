<?php require __DIR__ . '/_header.php'; ?>
    <div class="row">
        <div class="column">
            <div class="bloc bloc-number">
                <div class="number"><?php echo $sum->loc; ?></div>
                <div class="label">lines of code <?php echo $this->getTrend('sum', 'loc'); ?></div>
            </div>
        </div>
        <div class="column">
            <div class="bloc bloc-number">
                <div class="number"><?php echo $sum->nbClasses; ?></div>
                <div class="label">classes <?php echo $this->getTrend('sum', 'nbClasses'); ?></div>
            </div>
        </div>
        <div class="column">
            <div class="bloc bloc-number">
                <div
                    class="number"><?php echo $sum->nbClasses ? round($sum->nbMethods / $sum->nbClasses) : '-'; ?></div>
                <div class="label">methods by class</div>
            </div>
        </div>
        <div class="column">
            <div class="bloc bloc-number">
                <div class="number"><?php echo $sum->nbClasses ? round($sum->lloc / $sum->nbClasses) : '-'; ?></div>
                <div class="label">logical lines of code by class</div>
            </div>
        </div>
        <div class="column">
            <div class="bloc bloc-number">
                <div class="number"><?php echo $sum->nbMethods ? round($sum->lloc / $sum->nbMethods) : '-'; ?></div>
                <div class="label">logical lines of code by method</div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="column">
            <div class="bloc">
                <h4>Maintainability / complexity</h4>
                <div id="svg-maintainability"></div>
                <div class="help">
                    <p>Each file is symbolized by a circle. Size of the circle represents the Cyclomatic complexity.
                        Color
                        of the circle represents the Maintainability Index.</p>

                    <p>Large red circles will be probably hard to maintain.</p>
                </div>
            </div>
        </div>

        <div class="column column-75">
            <div class="bloc">
                <h4>Top 10 ClassRank <br /><small>(Google's page rank applyed to relations between classes)</small></h4>
                <table id="table-pagerank">
                    <thead>
                    <tr>
                        <th>Class</th>
                        <th>ClassRank</th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php
                    $classesS = $classes;
                    usort($classesS, function ($a, $b) {
                        return strcmp($b['pageRank'], $a['pageRank']);
                    });
                    $classesS = array_slice($classesS, 0, 10);
                    foreach ($classesS as $class) { ?>
                        <tr>
                            <td><?php echo $class['name']; ?> <span class="badge"
                                                                    title="Maintainability Index"><?php echo isset($class['mi']) ? $class['mi'] : ''; ?></span>
                            </td>
                            <td><?php echo $class['pageRank']; ?></td>
                        </tr>
                    <?php } ?>
                    </tbody>
                </table>

            </div>

        </div>
    </div>


    <script type="text/javascript">
        document.onreadystatechange = function () {
            if (document.readyState === 'complete') {
                chartMaintainability();
            }
        };
    </script>

<?php require __DIR__ . '/_footer.php'; ?>