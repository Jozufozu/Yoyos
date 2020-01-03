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

package com.jozufozu.yoyos.common

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.config.ModConfig

object YoyosConfig {
    @JvmStatic val spec: ForgeConfigSpec
    val general: General
    val vanillaYoyos: VanillaYoyos
    val botaniaYoyos: BotaniaYoyos

    init {
        val builder = ForgeConfigSpec.Builder()
        general = General(builder)
        vanillaYoyos = VanillaYoyos(builder)
        botaniaYoyos = BotaniaYoyos(builder)
        spec = builder.build()
    }

    class General(builder: ForgeConfigSpec.Builder) {
        val yoyoSwing: ForgeConfigSpec.BooleanValue

        val collectingBase: ForgeConfigSpec.IntValue

        val farmingYoyoReplant: ForgeConfigSpec.BooleanValue

        init {
            builder.push("General")

            yoyoSwing = builder
                    .comment("By default, yoyos pull the user back towards the yoyo when they get too far apart. Setting this to false disabled this behavior.")
                    .translation("yoyos.config.swing")
                    .define("yoyoSwing", true)

            collectingBase = builder
                    .comment("This number is doubled for every level of the collecting enchantment on a yoyo. It represents how many individual items a yoyo can pick up.")
                    .translation("yoyos.config.collecting_multiplier")
                    .defineInRange("collectingMultiplier", 64, 1, Int.MAX_VALUE)

            farmingYoyoReplant = builder
                    .comment("By default, farming/hoe yoyos replant whatever crop they harvest using any seeds that might have dropped. Setting this to false disables this behavior.")
                    .translation("yoyos.config.replant")
                    .define("farmingYoyoReplant", true)

            builder.pop()
        }
    }

    class VanillaYoyos(builder: ForgeConfigSpec.Builder) {
        val enabled: ForgeConfigSpec.BooleanValue

        val collectingEnabled: ForgeConfigSpec.BooleanValue
        val maxCollectingLevel: ForgeConfigSpec.IntValue

        val woodenYoyo: YoyoSettings
        val stoneYoyo: YoyoSettings
        val ironYoyo: YoyoSettings
        val goldYoyo: YoyoSettings
        val diamondYoyo: YoyoSettings
        val shearYoyo: YoyoSettings
        val hoeYoyo: YoyoSettings
        val stickyYoyo: YoyoSettings
        val creativeYoyo: YoyoSettings

        init {
            builder.push("Vanilla")

            enabled = builder
                    .comment("If set to false, all vanilla yoyos will be disabled, disregarding their individual settings.")
                    .translation("yoyos.config.vanilla_yoyos.enabled")
                    .define("enabled", true)

            collectingEnabled = builder
                    .comment("If set to false, removes the collecting enchantment from the game.")
                    .translation("yoyos.config.vanilla_yoyos.collecting_enabled")
                    .define("collectingEnabled", true)

            maxCollectingLevel = builder
                    .comment("If set to false, removes the collecting enchantment from the game.")
                    .translation("yoyos.config.vanilla_yoyos.collecting_enabled")
                    .defineInRange("maxCollectingLevel", 5, 1, Int.MAX_VALUE)

            woodenYoyo = YoyoSettings("Wooden", builder, 2.2, 6.0, 100, 3.0)
            stoneYoyo = YoyoSettings("Stone", builder, 4.0, 7.0, 200, 4.0)
            ironYoyo = YoyoSettings("Iron", builder, 5.0, 8.0, 300, 5.0)
            goldYoyo = YoyoSettings("Gold", builder, 5.5, 11.0, 600, 3.0)
            diamondYoyo = YoyoSettings("Diamond", builder, 1.7, 9.0, 400, 6.0)
            shearYoyo = YoyoSettings("Shear", builder, 5.1, 8.0, 300, 5.5)
            hoeYoyo = YoyoSettings("Yohoe", builder, 2.2, 9.0, 400, 6.5)
            stickyYoyo = YoyoSettings("Sticky", builder, 1.8, 9.0, 400, 0.0)
            creativeYoyo = YoyoSettings("Creative", builder, 0.9, 24.0, -1, 9001.0)
            builder.pop()
        }
    }

    class BotaniaYoyos(builder: ForgeConfigSpec.Builder) {
        val enabled: ForgeConfigSpec.BooleanValue

        val manasteelYoyo: YoyoSettings
        val elementiumYoyo: YoyoSettings
        val terrasteelYoyo: YoyoSettings

        init {
            builder.push("Vanilla")

            enabled = builder
                    .comment("If set to false, all botanical yoyos will be disabled, disregarding their individual settings.")
                    .translation("yoyos.config.vanilla_yoyos.enabled")
                    .define("enabled", true)

            manasteelYoyo = YoyoSettings("Iron", builder, 5.0, 8.0, 300, 5.0)
            elementiumYoyo = YoyoSettings("Diamond", builder, 1.7, 9.0, 400, 6.0)
            terrasteelYoyo = YoyoSettings("Diamond", builder, 1.7, 9.0, 400, 6.0)
            builder.pop()
        }
    }

    class YoyoSettings(name: String, builder: ForgeConfigSpec.Builder, weight: Double, length: Double, duration: Int, damage: Double) {
        val enabled: ForgeConfigSpec.BooleanValue
        val weight: ForgeConfigSpec.DoubleValue
        val length: ForgeConfigSpec.DoubleValue
        val duration: ForgeConfigSpec.IntValue
        val damage: ForgeConfigSpec.DoubleValue

        init {
            builder.push(name)

            this.enabled = builder
                    .comment("If set to false, this yoyo will not be registered")
                    .translation("yoyos.config.vanilla_yoyos.yoyo.enable")
                    .define("enabled", true)

            this.weight = builder
                    .comment("Affects how fast this yoyo moves.\nspeed = (distance to target) * min(1/weight, 1)")
                    .translation("yoyos.config.weight")
                    .defineInRange("weight", weight, 0.0, Double.MAX_VALUE)

            this.length = builder
                    .comment("The maximum distance this yoyo can be from its user")
                    .translation("yoyos.config.length")
                    .defineInRange("length", length, 0.0, Double.MAX_VALUE)

            this.duration = builder
                    .comment("The time (in ticks) after being thrown that this yoyo will be forced to retract.")
                    .translation("yoyos.config.duration")
                    .defineInRange("duration", duration, -1, Int.MAX_VALUE)

            this.damage = builder
                    .comment("How much damage this yoyo does when it hits an entity.")
                    .translation("yoyos.config.damage")
                    .defineInRange("damage", damage, 0.0, Double.MAX_VALUE)
            builder.pop()
        }
    }

    @JvmStatic fun onConfig(configEvent: ModConfig.ModConfigEvent) {
        configEvent.config.save()
    }
}
