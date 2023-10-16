package com.jozufozu.yoyos.core.control;

import org.joml.Vector3d;

public class YoyoContext {

    // How far away from the player we try to be.
    public final double targetDistance = 8;

    // The velocity of this yoyo. Persists between ticks and only changes when a force is applied.
    public final Vector3d velocity = new Vector3d();

    // The eye position of our owner.
    public final Vector3d eyePos = new Vector3d();

    // Where we should be.
    public final Vector3d targetPos = new Vector3d();

    // Where we are.
    public final Vector3d ourPos = new Vector3d();
}
