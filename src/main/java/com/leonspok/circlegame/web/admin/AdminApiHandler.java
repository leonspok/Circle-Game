package com.leonspok.circlegame.web.admin;

import com.leonspok.circlegame.GamesManager;
import com.leonspok.circlegame.logic.CircleGame;
import com.leonspok.circlegame.logic.CirclePlayer;

import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by igorsavelev on 05/03/16.
 */
@Path("/")
public class AdminApiHandler {
    @GET
    @Path("test")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public String test(@QueryParam("q") String query) {
        return "{\"admin_test\": \""+query+"\"}";
    }

    @POST
    @Path("login")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        if (GamesManager.getSharedInstance().isAdmin(username, password)) {
            NewCookie cookie = new NewCookie(AdminApiFilter.COOKIE_NAME, GamesManager.getSharedInstance().getAdminToken(), "/", null, 0, null, Integer.MAX_VALUE, false);
            return Response.ok().cookie(cookie).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @POST
    @Path("game/start")
    @Produces("application/json")
    public Response startGame() {
        GamesManager.getSharedInstance().createNewGame();
        GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
        return Response.ok().build();
    }

    @POST
    @Path("game/load")
    @Produces("application/json")
    public Response loadGame() {
        GamesManager.getSharedInstance().loadPreviousGame();
        if (GamesManager.getSharedInstance().getCurrentGame() != null) {
            GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
            return Response.ok().build();
        }
        GamesManager.getSharedInstance().createNewGame();
        GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
        return Response.ok().build();
    }

    @POST
    @Path("game/stop_registration")
    @Produces("application/json")
    public Response stopRegistration() {
        if (GamesManager.getSharedInstance().getCurrentGame().getAllPlayers().size() < 4) {
            return Response.ok().build();
        }
        GamesManager.getSharedInstance().getCurrentGame().stopRegistration();
        GamesManager.getSharedInstance().saveCurrentGame();
        GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
        return Response.ok().build();
    }

    @POST
    @Path("game/stop")
    @Produces("application/json")
    public Response stopGame() {
        GamesManager.getSharedInstance().stopCurrentGame();
        GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
        return Response.ok().build();
    }

    @POST
    @Path("players/kill")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response killPlayer(@FormParam("user_id") String userId) {
        if (userId == null || userId.isEmpty()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        CircleGame game = GamesManager.getSharedInstance().getCurrentGame();
        CirclePlayer player = game.getPlayerWithUID(userId);
        game.killPlayer(player);
        GamesManager.getSharedInstance().saveCurrentGame();
        GamesManager.getSharedInstance().sendPlayersListToAdmin();
        GamesManager.getSharedInstance().sendPlayerKilledStatus(player);
        return Response.ok().build();
    }

    @POST
    @Path("voting/start")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response startVoting(@FormParam("duration") Float duration) {
        if (duration == null || duration <= 10) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        GamesManager.getSharedInstance().startNewPoll(duration);
        GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
        for (CirclePlayer player : GamesManager.getSharedInstance().getCurrentGame().getAllPlayers()) {
            GamesManager.getSharedInstance().sendCurrentStatusForPlayer(player);
        }
        return Response.ok().build();
    }

    @POST
    @Path("voting/complete")
    @Produces("application/json")
    public Response completeVoting() {
        CircleGame game = GamesManager.getSharedInstance().getCurrentGame();
        if (game.getCurrentPoll() != null) {
            Set<CirclePlayer> playersToKill = new HashSet<CirclePlayer>();
            playersToKill.add(game.getCurrentPoll().getKilledUser());
            playersToKill.addAll(game.getCurrentPoll().getNotVotedUsers());
            game.killPlayers(playersToKill);
            game.closePoll();
            GamesManager.getSharedInstance().saveCurrentGame();

            GamesManager.getSharedInstance().sendCurrentStatusForAdmin();
            for (CirclePlayer player : playersToKill) {
                GamesManager.getSharedInstance().sendCurrentStatusForPlayer(player);
            }
            for (CirclePlayer player : game.getAllPlayers()) {
                GamesManager.getSharedInstance().sendCurrentStatusForPlayer(player);
            }
        }
        return Response.ok().build();
    }
}
