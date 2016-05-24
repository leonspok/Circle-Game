package com.leonspok.circlegame.logic;

import java.util.*;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class CirclePoll {
    private static final Random random = new Random(System.currentTimeMillis());
    public Map<CirclePlayer, Integer> results;
    public Set<CirclePlayer> voted;
    public Set<CirclePlayer> usersAllowedToVote;
    public boolean acceptEmptyVote;
    public int emptyVotes;
    private float duration;
    private boolean acceptVotes;
    private CirclePlayer killedUser;

    public CirclePoll(float duration, Set<CirclePlayer> usersAllowedToVote, Set<CirclePlayer> targetUsers, boolean acceptEmptyVote) {
        this.duration = duration;
        this.results = Collections.synchronizedMap(new HashMap<CirclePlayer, Integer>());
        for (CirclePlayer player : targetUsers) {
            this.results.put(player, new Integer(0));
        }
        this.voted = Collections.synchronizedSet(new HashSet<CirclePlayer>());
        this.usersAllowedToVote = Collections.synchronizedSet(usersAllowedToVote);
        this.acceptEmptyVote = acceptEmptyVote;
        this.emptyVotes = 0;

        this.acceptVotes = true;
    }

    public void addVote(CirclePlayer from, CirclePlayer to) {
        if (!this.acceptVotes || from == null || this.voted.contains(from) || (!this.acceptEmptyVote && to == null)) {
            return;
        }

        this.voted.add(from);
        if (to != null) {
            synchronized (this) {
                int votes = 0;
                if (this.results.containsKey(to)) {
                    votes = this.results.get(to);
                }
                votes++;
                this.results.put(to, votes);
            }
        } else {
            synchronized (this) {
                emptyVotes++;
            }
        }
    }

    public void completePoll() {
        this.acceptVotes = false;

        int minResult = Integer.MAX_VALUE;
        for (CirclePlayer player : this.results.keySet()) {
            int result = this.results.get(player);
            player.score += result;
            if (result < minResult) {
                minResult = result;
            }
        }

        List<CirclePlayer> usersWithMinResult = new LinkedList<CirclePlayer>();
        for (CirclePlayer player : this.results.keySet()) {
            int result = this.results.get(player);
            if (result <= minResult) {
                usersWithMinResult.add(player);
            }
        }

        if (usersWithMinResult.size() == 1) {
            this.killedUser = usersWithMinResult.get(0);
        } else if (usersWithMinResult.size() > 1) {
            int index = Math.abs(random.nextInt())%usersWithMinResult.size();
            this.killedUser = usersWithMinResult.get(index);
        }
    }

    public float getDuration() {
        return this.duration;
    }

    public CirclePlayer getKilledUser() {
        if (this.acceptVotes) {
            return null;
        }
        return this.killedUser;
    }

    public Set<CirclePlayer> getNotVotedUsers() {
        Set<CirclePlayer> notVotedUsers = new HashSet<CirclePlayer>();
        for (CirclePlayer player : this.usersAllowedToVote) {
            if (!this.voted.contains(player)) {
                notVotedUsers.add(player);
            }
        }
        return notVotedUsers;
    }
}
