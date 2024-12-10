package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.XPObeliskBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import com.enderio.machines.common.io.fluid.MachineFluidTank;
import com.enderio.machines.common.menu.base.MachineMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public class XPObeliskMenu extends MachineMenu<XPObeliskBlockEntity> {

    public XPObeliskMenu(int pContainerId, @Nullable XPObeliskBlockEntity blockEntity, Inventory inventory) {
        super(MachineMenus.XP_OBELISK.get(), pContainerId, blockEntity, inventory);
    }

    public static XPObeliskMenu factory(int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof XPObeliskBlockEntity castBlockEntity) {
            return new XPObeliskMenu(pContainerId, castBlockEntity, inventory);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new XPObeliskMenu(pContainerId, null, inventory);
    }

    public MachineFluidTank getFluidTank() {
        if (getBlockEntity() == null) {
            throw new IllegalStateException("BlockEntity is null");
        }

        return getBlockEntity().getFluidTank();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        XPObeliskBlockEntity blockEntity = getBlockEntity();
        if (blockEntity == null) {
            return false;
        }

        switch (id) {
        case 0 -> blockEntity.addLevelsToPlayer(player, 1);
        case 1 -> blockEntity.removeLevelsFromPlayer(player, 1);
        case 2 -> blockEntity.addLevelsToPlayer(player, 10);
        case 3 -> blockEntity.removeLevelsFromPlayer(player, 10);
        case 4 -> blockEntity.addAllXpToPlayer(player);
        case 5 -> blockEntity.removeAllXpFromPlayer(player);
        default -> throw new IllegalStateException("Unexpected value: " + id);
        }
        return true;
    }

}
