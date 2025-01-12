package com.enderio.conduits.common.conduit.menu;

import com.enderio.base.api.UseOnly;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.common.conduit.bundle.ConduitBundleBlockEntity;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.ConduitMenus;
import com.enderio.conduits.common.network.S2CConduitExtraGuiDataPacket;
import com.enderio.conduits.common.network.SetConduitConnectionConfigPacket;
import com.enderio.core.common.menu.BaseBlockEntityMenu;
import com.enderio.core.common.menu.BaseEnderMenu;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

// TODO: Make this not connect to the block entity at all, that way when the screen desyncs, the client world isn't desynced too.
// This means server menu should get/set connections direct from the BE but the client should have a standalone config store.
// Need to work out what this means for the client sync tag - it might need to be synced separately to the client from this GUI too.
// Possibly create an NBT sync slot and then use it for that?
public class ConduitMenu extends BaseEnderMenu {

    public static void openConduitMenu(ServerPlayer serverPlayer, BlockPos pos, ConduitBundleBlockEntity conduitBundle, Direction side,
        Holder<Conduit<?, ?>> conduit) {
        serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, player) ->
                new ConduitMenu(containerId, inventory, conduitBundle, side, conduit),
                conduit.value().description()),
            buf -> {
                buf.writeEnum(side);
                Conduit.STREAM_CODEC.encode(buf, conduit);
                ConnectionConfig.STREAM_CODEC.encode(buf, conduitBundle.getConnectionConfig(side, conduit));

                //noinspection DataFlowIssue
                ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG)
                    .map(opt -> opt.orElse(null), Optional::ofNullable)
                    .encode(buf, conduitBundle.getConduitExtraGuiData(side, conduit));
            });
    }

    public static final int BUTTON_CHANGE_CONDUIT_START_ID = 0;
    public static final int BUTTON_CHANGE_CONDUIT_ID_COUNT = ConduitBundleBlockEntity.MAX_CONDUITS;

    private final Direction side;
    private Holder<Conduit<?, ?>> selectedConduit; // TODO: Sync with sync slot instead of using initial menu open?

    private final ConnectionAccessor connectionAccessor;

    @UseOnly(LogicalSide.SERVER)
    private ConnectionConfig remoteConnectionConfig;

    @UseOnly(LogicalSide.SERVER)
    private CompoundTag remoteExtraGuiData;

    public ConduitMenu(int containerId, Inventory playerInventory, ConduitBundleBlockEntity conduitBundle, Direction side, Holder<Conduit<?, ?>> selectedConduit) {
        super(ConduitMenus.CONDUIT_MENU.get(), containerId, playerInventory);

        this.side = side;
        this.selectedConduit = selectedConduit;
        this.connectionAccessor = conduitBundle;
        this.remoteConnectionConfig = connectionAccessor.getConnectionConfig(side, selectedConduit);

        // TODO: Add conduit slots.

        addPlayerInventorySlots(23, 113);
    }

    public ConduitMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(ConduitMenus.CONDUIT_MENU.get(), containerId, playerInventory);

        side = buf.readEnum(Direction.class);
        selectedConduit = Conduit.STREAM_CODEC.decode(buf);
        this.connectionAccessor = new ClientConnectionAccessor(buf);

        // TODO: Add conduit slots.

        addPlayerInventorySlots(23, 113);
    }

    public Direction getSide() {
        return side;
    }

    public Holder<Conduit<?, ?>> getSelectedConduit() {
        return selectedConduit;
    }

    public ConnectionConfigType<?> connectionConfigType() {
        return selectedConduit.value().connectionConfigType();
    }

    public ConnectionConfig connectionConfig() {
        return connectionAccessor.getConnectionConfig(side, selectedConduit);
    }

    public <T extends ConnectionConfig> T connectionConfig(ConnectionConfigType<T> type) {
        var config = connectionConfig();
        if (config.type() == type) {
            //noinspection unchecked
            return (T) config;
        }

        throw new IllegalStateException("Connection config type mismatch");
    }

    public void setConnectionConfig(ConnectionConfig config) {
        connectionAccessor.setConnectionConfig(side, selectedConduit, config);
    }

    @Nullable
    public CompoundTag extraGuiData() {
        return connectionAccessor.getConduitExtraGuiData(side, selectedConduit);
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    public void setExtraGuiData(CompoundTag extraGuiData) {
        if (connectionAccessor instanceof ClientConnectionAccessor clientConnectionAccessor) {
            clientConnectionAccessor.extraGuiData = extraGuiData;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return connectionAccessor.stillValid(player) && connectionAccessor.canBeOrIsConnection(side, selectedConduit);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        //var bundle = getBlockEntity();
        //var currentConfig = connectionConfig();

        if (id >= BUTTON_CHANGE_CONDUIT_START_ID && id <= BUTTON_CHANGE_CONDUIT_ID_COUNT + BUTTON_CHANGE_CONDUIT_ID_COUNT) {
            // TODO: attempt to change to a different conduit on the same face.
            //var conduitList = getBlockEntity().getConduits();

            // TODO Find and switch to conduit and tell the client.
        }

        return super.clickMenuButton(player, id);
    }

    // TODO
    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (getPlayerInventory().player instanceof ServerPlayer serverPlayer) {
            if (!Objects.equals(connectionConfig(), remoteConnectionConfig)) {
                PacketDistributor.sendToPlayer(serverPlayer, new SetConduitConnectionConfigPacket(containerId, connectionConfig()));
                this.remoteConnectionConfig = connectionConfig();
            }

            var extraGuiData = extraGuiData();
            if (!Objects.equals(extraGuiData, remoteExtraGuiData)) {
                PacketDistributor.sendToPlayer(serverPlayer, new S2CConduitExtraGuiDataPacket(containerId, extraGuiData));
                this.remoteExtraGuiData = extraGuiData;
            }
        }
    }

    public interface ConnectionAccessor {
        // TODO: Conduit menu list.

        ConnectionConfig getConnectionConfig(Direction side, Holder<Conduit<?, ?>> conduit);
        void setConnectionConfig(Direction side, Holder<Conduit<?, ?>> conduit, ConnectionConfig config);
        boolean canBeOrIsConnection(Direction side, Holder<Conduit<?, ?>> conduit);

        @Nullable
        CompoundTag getConduitExtraGuiData(Direction side, Holder<Conduit<?, ?>> conduit);

        boolean stillValid(Player player);
    }

    private static class ClientConnectionAccessor implements ConnectionAccessor {

        private ConnectionConfig connectionConfig;

        @Nullable
        private CompoundTag extraGuiData;

        public ClientConnectionAccessor(RegistryFriendlyByteBuf buf) {
            this.connectionConfig = ConnectionConfig.STREAM_CODEC.decode(buf);
            extraGuiData = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG)
                .map(opt -> opt.orElse(null), Optional::ofNullable)
                .decode(buf);
        }

        @Override
        public ConnectionConfig getConnectionConfig(Direction side, Holder<Conduit<?, ?>> conduit) {
            return connectionConfig;
        }

        @Override
        public void setConnectionConfig(Direction side, Holder<Conduit<?, ?>> conduit, ConnectionConfig config) {
            connectionConfig = config;
        }

        @Override
        public boolean canBeOrIsConnection(Direction side, Holder<Conduit<?, ?>> conduit) {
            return true;
        }

        @Override
        public CompoundTag getConduitExtraGuiData(Direction side, Holder<Conduit<?, ?>> conduit) {
            return extraGuiData;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
