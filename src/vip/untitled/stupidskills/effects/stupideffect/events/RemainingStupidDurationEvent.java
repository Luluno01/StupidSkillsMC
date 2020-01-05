package vip.untitled.stupidskills.effects.stupideffect.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RemainingStupidDurationEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private int remainingStupidDuration;
    private int level;

    public RemainingStupidDurationEvent(Player player, int remainingStupidDuration, int level) {
        this.player = player;
        this.remainingStupidDuration = remainingStupidDuration;
        this.level = level;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getRemainingStupidDuration() {
        return remainingStupidDuration;
    }

    public void setRemainingStupidDuration(int remainingStupidDuration) {
        this.remainingStupidDuration = remainingStupidDuration;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
