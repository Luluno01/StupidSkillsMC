package vip.untitled.stupidskills.effects.stupideffect.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityStupefiedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private int duration;
    private int level;

    public EntityStupefiedEvent(int duration, int level) {
        this.duration = duration;
        this.level = level;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
