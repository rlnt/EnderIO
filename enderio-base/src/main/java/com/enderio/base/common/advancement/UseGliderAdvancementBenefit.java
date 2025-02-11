package com.enderio.base.common.advancement;

import com.enderio.EnderIOBase;
import com.enderio.base.api.EnderIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = EnderIOBase.MODULE_MOD_ID)
public class UseGliderAdvancementBenefit {

    public static final ResourceLocation USE_GLIDER_ADVANCEMENT = EnderIO.loc("adventure/use_glider");

    public static final Map<Integer, Item> PLAYER_BOUND_GLIDERS = new HashMap<>();

    @SubscribeEvent
    public static void onEarnAdvancement(AdvancementEvent.AdvancementEarnEvent earnAdvancement) {
        if (earnAdvancement.getAdvancement().id().equals(USE_GLIDER_ADVANCEMENT)) {
            Item item = PLAYER_BOUND_GLIDERS.get(earnAdvancement.getEntity().getUUID().hashCode());
            if (item != null && !earnAdvancement.getEntity().addItem(item.getDefaultInstance())) {
                earnAdvancement.getEntity().drop(item.getDefaultInstance(), false);
            }
        }
    }
}
