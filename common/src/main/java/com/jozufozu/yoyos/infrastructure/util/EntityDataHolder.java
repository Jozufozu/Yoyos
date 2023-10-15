package com.jozufozu.yoyos.infrastructure.util;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class EntityDataHolder<T> {

    private final SynchedEntityData entityData;
    private final EntityDataAccessor<T> dataAccessor;

    public EntityDataHolder(Yoyo yoyo, EntityDataAccessor<T> dataAccessor) {
        entityData = yoyo.getEntityData();
        this.dataAccessor = dataAccessor;
    }

    public T get() {
        return entityData.get(dataAccessor);
    }

    public void set(T obj) {
        entityData.set(dataAccessor, obj);
    }
}
