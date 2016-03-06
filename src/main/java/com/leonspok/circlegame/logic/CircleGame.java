package com.leonspok.circlegame.logic;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class CircleGame {
    private CircleGameStatus status;
    private Set<CirclePlayer> players;
    private CirclePoll currentPoll;

    public CircleGame() {
        this.status = CircleGameStatus.REGISTRATION;
        this.players = Collections.synchronizedSet(new HashSet<CirclePlayer>());
    }

    public static CircleGame fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }

        JSONArray players = (JSONArray)object.get("players");
        if (players == null) {
            return null;
        }

        CircleGame game = new CircleGame();
        for (int i = 0; i < players.size(); i++) {
            JSONObject jsonObject = (JSONObject)players.get(i);
            CirclePlayer player = CirclePlayer.fromJson(jsonObject);
            if (player != null) {
                game.players.add(player);
            }
        }
        game.status = CircleGameStatus.DISCUSSION;
        return game;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONArray playersJson = new JSONArray();
        for (CirclePlayer player : players) {
            playersJson.add(player.toJson());
        }
        object.put("players", playersJson);
        return object;
    }

    public CircleGameStatus getStatus() {
        return this.status;
    }

    public void stopRegistration() {
        this.status = CircleGameStatus.DISCUSSION;
    }

    public CirclePoll getCurrentPoll() {
        if (this.status == CircleGameStatus.VOTING || this.status == CircleGameStatus.VOTED) {
            return this.currentPoll;
        }
        return null;
    }

    public void createNextPoll(float duration) {
        if (this.status != CircleGameStatus.DISCUSSION) {
            return;
        }

        Set<CirclePlayer> aliveUsers = this.getAlivePlayers();
        boolean deadVoting = aliveUsers.size() <= 2;

        Set<CirclePlayer> allowedUsersToVote = this.getAlivePlayers();
        if (deadVoting) {
            allowedUsersToVote.addAll(this.getDeadPlayers());
        }

        this.currentPoll = new CirclePoll(duration, allowedUsersToVote, aliveUsers, !deadVoting);
        this.status = CircleGameStatus.VOTING;
    }

    public void completePoll() {
        this.status = CircleGameStatus.VOTED;
        this.currentPoll.completePoll();
    }

    public void closePoll() {
        this.status = CircleGameStatus.DISCUSSION;
        this.currentPoll = null;
        if (this.getAlivePlayers().size() <= 1) {
            this.status = CircleGameStatus.FINISHED;
        }
    }

    public void closeGame() {
        this.status = CircleGameStatus.FINISHED;
    }

    public synchronized CirclePlayer addPlayer(String name) {
        if (this.status != CircleGameStatus.REGISTRATION || name == null || name.isEmpty()) {
            return null;
        }

        CirclePlayer player = CirclePlayer.createNew(name, this.players.size()+1);
        this.players.add(player);
        return player;
    }

    public CirclePlayer getPlayerWithUID(String uid) {
        if (uid == null || uid.isEmpty()) {
            return null;
        }

        for (CirclePlayer player : this.players) {
            if (player.uid.equals(uid)) {
                return player;
            }
        }
        return null;
    }

    public void killPlayer(CirclePlayer player) {
        player.alive = false;
    }

    public void killPlayers(Set<CirclePlayer> players) {
        for (CirclePlayer player : players) {
            player.alive = false;
        }
    }

    public Set<CirclePlayer> getAllPlayers() {
        return this.players;
    }

    public Set<CirclePlayer> getAlivePlayers() {
        Set<CirclePlayer> alivePlayers = new HashSet<CirclePlayer>();
        for (CirclePlayer player : this.players) {
            if (player.alive) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }

    public Set<CirclePlayer> getDeadPlayers() {
        Set<CirclePlayer> deadPlayers = new HashSet<CirclePlayer>();
        for (CirclePlayer player : this.players) {
            if (!player.alive) {
                deadPlayers.add(player);
            }
        }
        return deadPlayers;
    }
}
