package com.jozufozu.yoyos.infrastructure.register.packet;

import com.jozufozu.yoyos.infrastructure.register.Entry;
import com.jozufozu.yoyos.infrastructure.register.Register;

public class PacketEntry<T> extends Entry<PacketBehavior<T>> {
    public PacketEntry(Register.Promise<PacketBehavior<T>> promise) {
        super(promise);
    }
}
