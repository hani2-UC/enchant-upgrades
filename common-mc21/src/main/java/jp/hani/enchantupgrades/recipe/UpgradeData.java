package jp.hani.enchantupgrades.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record UpgradeData(List<UpgradeRecipe> recipes, int revision) {
    public UpgradeData { recipes = List.copyOf(recipes); }
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeData> STREAM_CODEC = new StreamCodec<>() {
        @Override public void encode(RegistryFriendlyByteBuf buf, UpgradeData data) {
            buf.writeVarInt(data.revision);
            buf.writeVarInt(data.recipes.size());
            for (UpgradeRecipe r : data.recipes) {
                buf.writeResourceLocation(r.id());
                buf.writeResourceLocation(r.enchantmentHolder().unwrapKey().orElseThrow().location());
                buf.writeVarInt(r.material().items().size());
                for (ItemStack item : r.material().items()) buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item.getItem()));
                buf.writeVarInt(r.baseMaterial()).writeVarInt(r.materialStep()).writeVarInt(r.baseLevels())
                    .writeVarInt(r.levelStep()).writeVarInt(r.baseShelves()).writeVarInt(r.shelfStep());
            }
        }
        @Override public UpgradeData decode(RegistryFriendlyByteBuf buf) {
            int revision = buf.readVarInt();
            int count = boundedCount(buf.readVarInt());
            var enchants = buf.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            List<UpgradeRecipe> recipes = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                var id = buf.readResourceLocation();
                var enchant = enchants.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, buf.readResourceLocation()));
                int materialCount = boundedCount(buf.readVarInt());
                List<ItemStack> items = new ArrayList<>();
                for (int j = 0; j < materialCount; j++) items.add(new ItemStack(BuiltInRegistries.ITEM.getOptional(buf.readResourceLocation()).orElseThrow()));
                recipes.add(new UpgradeRecipe(id, enchant, new UpgradeMaterial(items), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
            }
            return new UpgradeData(recipes, revision);
        }
        private int boundedCount(int count) {
            if (count < 0 || count > 4096) throw new IllegalArgumentException("Invalid upgrade snapshot size");
            return count;
        }
    };
}
