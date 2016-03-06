package com.leonspok.circlegame;

import com.leonspok.circlegame.logic.CircleGame;
import com.leonspok.circlegame.logic.CirclePlayer;
import com.leonspok.circlegame.web.admin.AdminSocket;
import com.leonspok.circlegame.web.admin.AdminStatus;
import com.leonspok.circlegame.web.players.PlayerSocket;
import com.leonspok.circlegame.web.players.PlayerStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class GamesManager {
    private static final GamesManager sharedInstance = new GamesManager();
    private static final String ADMIN_TOKEN = "iamadminbitches";
    private Map<CirclePlayer, PlayerSocket> playerSockets;
    private AdminSocket adminSocket;
    private CircleGame currentGame;
    private GameTimer pollTimer;

    public GamesManager() {
        this.playerSockets = Collections.synchronizedMap(new HashMap<CirclePlayer, PlayerSocket>());
    }


    public static GamesManager getSharedInstance() {
        return sharedInstance;
    }

    private String pathToSaveFile() {
        String homeFolder = System.getProperty("user.home");
        return homeFolder+"/"+"circlegame.json";
    }

    public void saveCurrentGame() {
        if (this.currentGame == null) {
            return;
        }

        String jsonString = this.currentGame.toJson().toJSONString();
        try {
            FileWriter out = new FileWriter(this.pathToSaveFile());
            out.write(jsonString);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void loadPreviousGame() {
        File file = new File(this.pathToSaveFile());
        if (file.exists()) {
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(new FileReader(this.pathToSaveFile()));
                JSONObject jsonObject = (JSONObject) obj;
                this.currentGame = CircleGame.fromJson(jsonObject);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void stopCurrentGame() {
        this.pollTimer.cancel();
        this.currentGame.closePoll();
        for (PlayerSocket socket : this.playerSockets.values()) {
            socket.close();
        }
        this.playerSockets.clear();
        this.currentGame = null;
    }

    public void createNewGame() {
        this.currentGame = new CircleGame();
    }

    public CircleGame getCurrentGame() {
        return this.currentGame;
    }

    public void startNewPoll(float duration) {
        this.currentGame.createNextPoll(duration);
        this.pollTimer = new GameTimer(duration) {
            @Override
            public void timerFired() {
                //TODO: send time to admin
            }

            @Override
            public void timerCompleted() {
                System.out.println("Timer completed");
                GamesManager.getSharedInstance().getCurrentGame().completePoll();
                GamesManager.getSharedInstance().sendVotedStatusToAdmin();
                GamesManager.getSharedInstance().sendFinalVotingResultsToAdmin();
            }
        };
        this.pollTimer.start();
    }

    public boolean userExistsWithToken(String token) {
        return this.currentGame.getPlayerWithUID(token) != null;
    }

    public boolean isAdmin(String username, String password) {
        if (username.equals("vasilyev") && password.equals("ogon")) {
            return true;
        }
        return false;
    }

    public String getAdminToken() {
        return ADMIN_TOKEN;
    }

    public synchronized void addPlayerSocket(PlayerSocket socket, CirclePlayer player) {
        if (this.playerSockets.containsKey(player)) {
            PlayerSocket oldSocket = this.playerSockets.get(player);
            if (oldSocket != socket) {
                oldSocket.close();
            }
        }
        this.playerSockets.put(player, socket);
        this.sendCurrentStatusForPlayer(player);
    }

    public void sendCurrentStatusForPlayer(CirclePlayer player) {
        switch (this.currentGame.getStatus()) {
            case REGISTRATION:
                this.sendPlayerDiscussionStatus(player);
                break;
            case DISCUSSION:
                if (player.alive) {
                    this.sendPlayerDiscussionStatus(player);
                } else {
                    this.sendPlayerKilledStatus(player);
                }
                break;
            case VOTED:
                this.sendPlayerVotedStatus(player);
                break;
            case VOTING:
                if (this.currentGame.getAlivePlayers().size() <= 2 || player.alive) {
                    if (this.currentGame.getCurrentPoll().voted.contains(player)) {
                        if (player.alive) {
                            this.sendPlayerVotedStatus(player);
                        } else {
                            this.sendPlayerKilledStatus(player);
                        }
                    } else {
                        this.sendPlayerVotingStatus(player);
                    }
                } else {
                    this.sendPlayerKilledStatus(player);
                }
                break;
            case FINISHED:
                break;
        }
    }

    public synchronized void removePlayerSocket(PlayerSocket socket, CirclePlayer player) {
        if (this.playerSockets.containsKey(player)) {
            PlayerSocket oldSocket = this.playerSockets.get(player);
            if (oldSocket.equals(socket)) {
                this.playerSockets.remove(player);
            }
        }
    }

    public void sendPlayerDiscussionStatus(CirclePlayer player) {
        PlayerSocket socket = this.playerSockets.get(player);
        if (socket != null) {
            socket.sendStatus(PlayerStatus.DISCUSSION, null);
        }
    }

    public void sendPlayerVotedStatus(CirclePlayer player) {
        PlayerSocket socket = this.playerSockets.get(player);
        if (socket != null) {
            socket.sendStatus(PlayerStatus.VOTED, null);
        }
    }

    public void sendPlayerKilledStatus(CirclePlayer player) {
        PlayerSocket socket = this.playerSockets.get(player);
        if (socket != null) {
            socket.sendStatus(PlayerStatus.KILLED, null);
        }
    }

    public void sendPlayerVotingStatus(CirclePlayer player) {
        PlayerSocket socket = this.playerSockets.get(player);
        if (socket != null) {
            JSONObject params = new JSONObject();
            Set<CirclePlayer> votingOptions = new HashSet<CirclePlayer>(this.currentGame.getCurrentPoll().results.keySet());
            if (votingOptions.contains(player)) {
                votingOptions.remove(player);
            }
            JSONArray players = new JSONArray();
            for (CirclePlayer pl : votingOptions) {
                players.add(pl.toJson());
            }
            Collections.shuffle(players);
            params.put("players", players);
            params.put("duration", this.currentGame.getCurrentPoll().getDuration());
            params.put("accept_empty_vote", this.currentGame.getCurrentPoll().acceptEmptyVote);
            socket.sendStatus(PlayerStatus.VOTING, params);
        }
    }

    public synchronized void setAdminSocket(AdminSocket socket) {
        if (this.adminSocket != null) {
            this.adminSocket.close();
            this.adminSocket = null;
        }
        this.adminSocket = socket;
        this.sendCurrentStatusForAdmin();
    }

    public void sendCurrentStatusForAdmin() {
        if (this.currentGame != null) {
            System.out.println(this.currentGame.getStatus().toString());
            switch (this.currentGame.getStatus()) {
                case REGISTRATION:
                    this.sendRegistrationStatusToAdmin();
                    break;
                case DISCUSSION:
                    this.sendDiscussionStatusToAdmin();
                    break;
                case VOTED:
                    this.sendVotedStatusToAdmin();
                    this.sendFinalVotingResultsToAdmin();
                    break;
                case VOTING:
                    this.sendVotingStatusToAdmin();
                    this.sendVotingResultsToAdmin();
                    break;
                case FINISHED:
                    this.sendFinishedStatusToAdmin();
                    break;
            }
            this.sendPlayersListToAdmin();
        } else {
            this.sendNoGameStatusToAdmin();
        }
    }

    public synchronized void removeAdminSocket() {
        this.adminSocket = null;
    }

    public void sendVotingResultsToAdmin() {
        if (this.adminSocket != null) {
            Set<CirclePlayer> votingOptions = new HashSet<CirclePlayer>(this.currentGame.getCurrentPoll().results.keySet());
            ArrayList<CirclePlayer> sorted = new ArrayList<CirclePlayer>(votingOptions);
            Collections.sort(sorted, new Comparator<CirclePlayer>() {
                public int compare(CirclePlayer o1, CirclePlayer o2) {
                    if (o1.number > o2.number) {
                        return 1;
                    } else if (o1.number < o2.number) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            JSONObject params = new JSONObject();
            JSONArray results = new JSONArray();
            for (CirclePlayer player : sorted) {
                JSONObject object = new JSONObject();
                object.put("votes", this.currentGame.getCurrentPoll().results.get(player));
                object.put("type", "player");
                object.put("player", player.toJson());
                results.add(object);
            }
            if (this.currentGame.getCurrentPoll().acceptEmptyVote) {
                JSONObject object = new JSONObject();
                object.put("votes", this.currentGame.getCurrentPoll().emptyVotes);
                object.put("type", "nobody");
                results.add(object);
            }
            params.put("results", results);
            params.put("max_votes", this.currentGame.getCurrentPoll().usersAllowedToVote.size());

            this.adminSocket.sendAction("update_voting_results", params);
        }
    }

    public void sendFinalVotingResultsToAdmin() {
        if (this.adminSocket != null) {
            CirclePlayer killedPlayer = this.currentGame.getCurrentPoll().getKilledUser();
            Set<CirclePlayer> notVotedPlayers = this.currentGame.getCurrentPoll().getNotVotedUsers();

            JSONObject params = new JSONObject();
            params.put("killed_player", killedPlayer.toJson());
            JSONArray notVoted = new JSONArray();
            for (CirclePlayer player : notVotedPlayers) {
                notVoted.add(player.toJson());
            }
            params.put("not_voted", notVoted);
            this.adminSocket.sendAction("display_final_voting_results", params);
        }
    }

    public void sendPlayersListToAdmin() {
        if (this.adminSocket != null) {
            Set<CirclePlayer> players = this.currentGame.getAllPlayers();
            ArrayList<CirclePlayer> sorted = new ArrayList<CirclePlayer>(players);
            Collections.sort(sorted, new Comparator<CirclePlayer>() {
                public int compare(CirclePlayer o1, CirclePlayer o2) {
                    if (o1.number > o2.number) {
                        return 1;
                    } else if (o1.number < o2.number) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            JSONObject params = new JSONObject();
            JSONArray playersJson = new JSONArray();
            for (CirclePlayer player : sorted) {
                playersJson.add(player.toJson());
            }
            params.put("players", playersJson);
            this.adminSocket.sendAction("update_players_list", params);
        }
    }

    public void sendNoGameStatusToAdmin() {
        if (this.adminSocket != null) {
            this.adminSocket.sendStatus(AdminStatus.NO_GAME, null);
        }
    }

    public void sendRegistrationStatusToAdmin() {
        if (this.adminSocket != null) {
            this.adminSocket.sendStatus(AdminStatus.REGISTRATION, null);
        }
    }

    public void sendDiscussionStatusToAdmin() {
        if (this.adminSocket != null) {
            this.adminSocket.sendStatus(AdminStatus.DISCUSSION, null);
        }
    }

    public void sendVotingStatusToAdmin() {
        if (this.adminSocket != null) {
            this.adminSocket.sendStatus(AdminStatus.VOTING, null);
        }
    }

    public void sendVotedStatusToAdmin() {
        if (this.adminSocket != null) {
            this.adminSocket.sendStatus(AdminStatus.VOTED, null);
        }
    }

    public void sendFinishedStatusToAdmin() {
        if (this.adminSocket != null) {
            this.adminSocket.sendStatus(AdminStatus.FINISHED, null);
        }
    }

}
