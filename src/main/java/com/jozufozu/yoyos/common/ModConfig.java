/*
 * Copyright (c) 2018 Jozsef Augusztiny
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jozufozu.yoyos.common;

public class ModConfig
{
    public static boolean yoyoSwing = true;

    public static int collectingBase = 64;

    public static VanillaYoyos vanillaYoyos = new VanillaYoyos();

    public static BotaniaYoyos botaniaYoyos = new BotaniaYoyos();

    public static boolean replant = true;

    public static boolean tinkersYoyos = true;

    public static boolean stationCrafting = false;

    public static boolean configMaterials = false;

    public static class VanillaYoyos
    {
        public boolean enable = true;

        public YoyoSettings woodenYoyo = new YoyoSettings(2.2f, 6.0f, 100, 3.0f);
        public YoyoSettings stoneYoyo = new YoyoSettings(4.0f, 7.0f, 200, 4.0f);
        public YoyoSettings ironYoyo = new YoyoSettings(5.0f, 8.0f, 300, 5.0f);
        public YoyoSettings shearYoyo = new YoyoSettings(5.1f, 8.0f, 300, 5.5f);
        public YoyoSettings goldYoyo = new YoyoSettings(5.5f, 11.0f, 600, 3.0f);
        public YoyoSettings diamondYoyo = new YoyoSettings(1.7f, 9.0f, 400, 6.0f);
        public YoyoSettings hoeYoyo = new YoyoSettings(2.2f, 9.0f, 400, 6.5f);
        public YoyoSettings stickyYoyo = new YoyoSettings(1.8f, 9.0f, 400, 0.0f);
        public YoyoSettings creativeYoyo = new YoyoSettings(0.9f, 24.0f, -1, 9001.0f);
    }

    public static class BotaniaYoyos
    {
        public boolean enable = true;

        public boolean lexiconHackery = true;

        public YoyoSettings manasteelYoyo = new YoyoSettings(5.0f, 8.0f, 300, 5.0f);
        public YoyoSettings elementiumYoyo = new YoyoSettings(5.0f, 8.0f, 300, 5.0f);
        public YoyoSettings terrasteelYoyo = new YoyoSettings(1.7f, 9.0f, 400, 6.0f);
    }

    public static class YoyoSettings
    {
        public float weight;

        public float length;

        public int duration;

        public double damage;

        public YoyoSettings(float weight, float length, int duration, double damage)
        {
            this.weight = weight;
            this.length = length;
            this.duration = duration;
            this.damage = damage;
        }
    }
}
