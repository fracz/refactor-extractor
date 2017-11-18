package org.neo4j.graphalgo;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

public interface PathFinder
{
    /**
     * Tries to find a single path between {@code start} and {@code end}
     * nodes. If a path is found a {@link Path} is returned with that path
     * information, else {@code null} is returned. If more than one path is
     * found, the implementation can decide itself upon which of those to return.
     *
     * @param start the start {@link Node} which defines the start of the path.
     * @param end the end {@link Node} which defines the end of the path.
     * @return a single {@link Path} between {@code start} and {@code end},
     * or {@code null} if no path was found.
     */
    Path findSinglePath( Node start, Node end );

    /**
     * Tries to find all paths between {@code start} and {@code end} nodes.
     * A collection of {@link Path}s is returned with all the found paths.
     * If no paths are found an empty collection is returned.
     *
     * @param start the start {@link Node} which defines the start of the path.
     * @param end the end {@link Node} which defines the end of the path.
     * @return all {@link Path}s between {@code start} and {@code end}.
     */
    Iterable<Path> findAllPaths( Node start, Node end );
}