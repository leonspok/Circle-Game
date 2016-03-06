package com.leonspok.circlegame.web.players;

import com.leonspok.circlegame.GamesManager;
import com.leonspok.circlegame.logic.CircleGame;
import com.leonspok.circlegame.logic.CircleGameStatus;
import com.leonspok.circlegame.logic.CirclePlayer;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * Created by igorsavelev on 05/03/16.
 */

@Path("/")
public class ApiHandler {
    @GET
    @Path("test")
    @Produces("application/json")
    public String test(@QueryParam("q") String query) {
        return "{\"test\": \""+query+"\"}";
    }

    @POST
    @Path("login")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response login(@FormParam("name") String name) {
        if (name == null || name.isEmpty()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        if (GamesManager.getSharedInstance().getCurrentGame() == null) {
            return Response.ok().build();
        }

        CirclePlayer player = GamesManager.getSharedInstance().getCurrentGame().addPlayer(name);
        if (player != null) {
            NewCookie cookie = new NewCookie(ApiFilter.COOKIE_NAME, player.uid, "/", null, 0, null, Integer.MAX_VALUE, false);
            JSONObject response = new JSONObject();
            response.put("user_id", player.uid);
            response.put("number", player.number);
            GamesManager.getSharedInstance().sendPlayersListToAdmin();
            return Response.ok(response.toJSONString()).cookie(cookie).build();
        } else {
            return Response.ok().build();
        }
    }

    @POST
    @Path("vote")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response vote(@FormParam("user_id") String userId, @CookieParam(ApiFilter.COOKIE_NAME) String fromUserId) {
        CircleGame game = GamesManager.getSharedInstance().getCurrentGame();
        if (game.getStatus() == CircleGameStatus.VOTING) {
            CirclePlayer from = game.getPlayerWithUID(fromUserId);
            CirclePlayer to = game.getPlayerWithUID(userId);
            if (GamesManager.getSharedInstance().getCurrentGame().getCurrentPoll() == null) {
                Response.ok().build();
            }
            GamesManager.getSharedInstance().getCurrentGame().getCurrentPoll().addVote(from, to);
            GamesManager.getSharedInstance().sendVotingResultsToAdmin();

            if (from.alive) {
                GamesManager.getSharedInstance().sendPlayerVotedStatus(from);
            } else {
                GamesManager.getSharedInstance().sendPlayerKilledStatus(from);
            }
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @POST
    @Path("empty_vote")
    @Produces("application/json")
    public Response vote(@CookieParam(ApiFilter.COOKIE_NAME) String fromUserId) {
        CircleGame game = GamesManager.getSharedInstance().getCurrentGame();
        if (game.getStatus() == CircleGameStatus.VOTING) {
            CirclePlayer from = game.getPlayerWithUID(fromUserId);
            if (GamesManager.getSharedInstance().getCurrentGame().getCurrentPoll() == null) {
                Response.ok().build();
            }
            GamesManager.getSharedInstance().getCurrentGame().getCurrentPoll().addVote(from, null);
            GamesManager.getSharedInstance().sendVotingResultsToAdmin();

            if (from.alive) {
                GamesManager.getSharedInstance().sendPlayerVotedStatus(from);
            } else {
                GamesManager.getSharedInstance().sendPlayerKilledStatus(from);
            }
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}
