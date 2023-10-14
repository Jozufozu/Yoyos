package com.jozufozu.yoyos.core.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.Entity;

public class CollisionCollector implements Iterable<Entity> {
    private final List<Entity> entities = new ArrayList<>();

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<Entity> iterator() {
        return entities.iterator();
    }

    public void markHit(List<Entity> entities) {
        this.entities.addAll(entities);
    }
}
