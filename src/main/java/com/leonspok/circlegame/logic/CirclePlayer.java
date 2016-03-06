package com.leonspok.circlegame.logic;

import org.json.simple.JSONObject;

import java.util.Random;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class CirclePlayer {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String uid;
    public String name;
    public int number;
    public boolean alive;


    public static CirclePlayer createNew(String name, int number) {
        CirclePlayer player = new CirclePlayer();
        player.uid = generateUID();
        player.alive = true;
        player.number = number;
        player.name = name;
        return player;
    }

    private static String generateUID() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = Math.abs(random.nextInt())%letters.length();
            char ch = letters.charAt(index);
            sb.append(ch);
        }
        return sb.toString();
    }

    public static CirclePlayer fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        CirclePlayer player = new CirclePlayer();
        player.uid = (String)object.get("uid");
        player.name = (String)object.get("name");
        player.alive = (Boolean)object.get("alive");
        player.number = ((Long)object.get("number")).intValue();
        return player;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        object.put("uid", this.uid);
        object.put("name", this.name);
        object.put("alive", this.alive);
        object.put("number", this.number);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CirclePlayer that = (CirclePlayer) o;

        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
