package org.mockito.release.notes.vcs;

import org.mockito.release.notes.util.Predicate;

import java.util.*;

class DefaultContributionSet implements ContributionSet {
    private final Map<String, DefaultContribution> contributions = new HashMap<String, DefaultContribution>();
    private final Collection<Commit> commits = new LinkedList<Commit>();
    private final Predicate<Commit> ignoreCommit;

    public DefaultContributionSet(Predicate<Commit> ignoredCommit) {
        this.ignoreCommit = ignoredCommit;
    }

    void add(Commit commit) {
        if (ignoreCommit.isTrue(commit)) {
            return;
        }
        commits.add(commit);
        DefaultContribution c = contributions.get(commit.getAuthorId());
        if (c == null) {
            contributions.put(commit.getAuthorId(), new DefaultContribution(commit));
        } else {
            c.add(commit);
        }
    }

    public Collection<Commit> getAllCommits() {
        return commits;
    }

    public String toText() {
        StringBuilder sb = new StringBuilder("* Authors: ").append(contributions.size())
                .append("\n* Commits: ").append(commits.size());

        for (Map.Entry<String, DefaultContribution> entry : sortByValue(contributions)) {
            Contribution c = entry.getValue();
            sb.append("\n  * ").append(c.toText());
        }

        return sb.toString();
    }

    public static <K, V extends Comparable<V>> List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(entries, new ByValue<K, V>());
        return entries;
    }

    private static class ByValue<K, V extends Comparable<V>> implements Comparator<Map.Entry<K, V>> {
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}