package vip.untitled.stupidskills.effects.stupideffect.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StupidPlayerExitEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private int remainingStupidDuration;

    public StupidPlayerExitEvent(Player player, int remainingStupidDuration) {
        this.player = player;
        this.remainingStupidDuration = remainingStupidDuration;
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
}
