package com.jozufozu.yoyos.tinkers.materials;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;

public class YoyoNBT extends ToolNBT {

    public static final String LOC_Duration = "stat.yoyo.duration.name";
    public static final String LOC_Suffix = "stat.yoyo.duration.suffix";
    public static final String LOC_Infinite = "stat.yoyo.infinite.name";

    public static final String CHORD_LENGTH = "ChordLength";
    public static final String DURATION = "Duration";
    public static final String WEIGHT = "Weight";

    public float weight;
    public float chordLength;    //in meters
    public int duration = 100;   //the amount of time in ticks the yoyo will be alive

    //Both used to calculate duration
    private float friction;
    private float leastFriction = -50;

    private void setLeastFriction(float leastFriction) {
        if (this.leastFriction == -50)
            this.leastFriction = leastFriction;
        this.leastFriction = Math.min(leastFriction, this.leastFriction);
    }

    public YoyoNBT() {
    }

    public YoyoNBT(NBTTagCompound tag) {
        read(tag);
    }

    /**
     * Run this first
     * @param sides All sides the yoyo will have
     * @return this
     */
    public YoyoNBT side(BodyMaterialStats... sides) {
        for (BodyMaterialStats stat : sides) {
            this.attack += stat.attack;
            this.durability += stat.durability;
            this.weight += stat.weight;
        }

        return this;
    }

    /**
     * Run this second
     * @param cores All cores the yoyo will have, normally one
     * @return this
     */
    public YoyoNBT core(AxleMaterialStats... cores) {

        for (AxleMaterialStats core : cores) {
            this.friction += core.friction;
            setLeastFriction(core.friction);

            this.durability *= core.modifier;
        }

        return this;
    }

    /**
     * Run this last
     * @param chords All chords the yoyo will have, normally one
     * @return this
     */
    public YoyoNBT chord(CordMaterialStats... chords) {
        for (CordMaterialStats chord : chords) {
            this.chordLength += chord.length;

            this.friction += chord.friction;
            setLeastFriction(chord.friction);
        }

        if (this.leastFriction <= 0)
            this.duration = -1;
        else
            this.duration /= friction;
        this.chordLength = Math.min(Math.max(this.chordLength, 2), 32);
        return this;
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);
        this.weight = tag.getFloat(WEIGHT);
        this.chordLength = tag.getFloat(CHORD_LENGTH);
        this.duration = tag.getInteger(DURATION);
    }

    @Override
    public void write(NBTTagCompound tag) {
        super.write(tag);
        tag.setFloat(WEIGHT, this.weight);
        tag.setFloat(CHORD_LENGTH, this.chordLength);
        tag.setInteger(DURATION, this.duration);
    }

    public static YoyoNBT from(ItemStack itemStack) {
        return new YoyoNBT(TagUtil.getToolTag(itemStack));
    }
}
