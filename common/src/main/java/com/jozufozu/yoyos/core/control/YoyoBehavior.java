package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

public interface YoyoBehavior extends CollisionListener {
    void tick(Yoyo yoyo);
}
