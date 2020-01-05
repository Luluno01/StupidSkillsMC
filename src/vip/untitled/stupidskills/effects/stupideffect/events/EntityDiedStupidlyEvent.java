package vip.untitled.stupidskills.effects.stupideffect.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityDiedStupidlyEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private LivingEntity entity;
    private int level;

    public EntityDiedStupidlyEvent(LivingEntity entity, int level) {
        this.entity = entity;
        this.level = level;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(Player entity) {
        this.entity = entity;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
