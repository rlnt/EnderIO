package com.enderio.base.common.menu;

import com.enderio.base.api.attachment.StoredEntityData;
import com.enderio.base.common.capability.EntityFilterCapability;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOMenus;
import com.enderio.base.common.network.FilterUpdatePacket;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntityFilterMenu extends AbstractContainerMenu {

    private final ItemStack stack;
    private final EntityFilterCapability capability;

    public EntityFilterMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, ItemStack stack) {
        super(pMenuType, pContainerId);
        this.stack = stack;

        var resourceFilter = stack.getCapability(EIOCapabilities.Filter.ITEM);
        if (!(resourceFilter instanceof EntityFilterCapability filterCapability)) {
            throw new IllegalArgumentException();
        }

        capability = filterCapability;

        List<StoredEntityData> items = capability.getEntries();
        for (int i = 0; i < items.size(); i++) {
            int pSlot = i;
            addSlot(new EntityFilterSlot(data -> capability.setEntry(pSlot, data), i, 14 + (i % 5) * 18,
                    35 + 20 * (i / 5)));
        }
        addInventorySlots(14, 119, inventory);
    }

    public EntityFilterMenu(int pContainerId, Inventory inventory, ItemStack stack) {
        this(EIOMenus.ENTITY_FILTER.get(), pContainerId, inventory, stack);
    }

    public static EntityFilterMenu factory(int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        return new EntityFilterMenu(EIOMenus.ENTITY_FILTER.get(), pContainerId, inventory,
                inventory.player.getMainHandItem());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return pPlayer.getMainHandItem().equals(stack);
    }

    public void addInventorySlots(int xPos, int yPos, Inventory inventory) {

        // Hotbar
        for (int x = 0; x < 9; x++) {
            Slot ref = new Slot(inventory, x, xPos + x * 18, yPos + 58) {

                @Override
                public boolean mayPickup(Player player) {
                    return !this.getItem().equals(EntityFilterMenu.this.stack);
                }
            };
            this.addSlot(ref);
        }

        // Inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Slot ref = new Slot(inventory, x + y * 9 + 9, xPos + x * 18, yPos + y * 18) {

                    @Override
                    public boolean mayPickup(Player player) {
                        return !this.getItem().equals(EntityFilterMenu.this.stack);
                    }
                };
                this.addSlot(ref);
            }
        }

    }

    public EntityFilterCapability getFilter() {
        return capability;
    }

    public void setNbt(Boolean nbt) {
        PacketDistributor.sendToServer(new FilterUpdatePacket(nbt, capability.isInvert()));
        capability.setNbt(nbt);
    }

    public void setInverted(Boolean inverted) {
        PacketDistributor.sendToServer(new FilterUpdatePacket(capability.isNbt(), inverted));
        capability.setInverted(inverted);
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (pSlotId < capability.getEntries().size() && pSlotId >= 0) {
            if (capability.getEntries().get(pSlotId).hasEntity()) {
                capability.setEntry(pSlotId, StoredEntityData.EMPTY);
            }
        }
        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    public void doClick(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < capability.size()) {
            // Only allow PICKUP (click) or QUICK_MOVE (shift + click) events.
            if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
                return;
            }

            if (capability.getEntry(slotId).hasEntity()) {
                capability.setEntry(slotId, StoredEntityData.EMPTY);
            }
        }

        super.doClick(slotId, button, clickType, player);
    }
}
