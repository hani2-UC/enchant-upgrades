package jp.hani.enchantupgrades.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UpgradeRepository extends SimplePreparableReloadListener<List<UpgradeRepository.Spec>> {
    private static volatile List<Spec> recipes = List.of();
    private static volatile int revision;
    public static int revision() { return revision; }
    record Spec(ResourceLocation id, ResourceLocation enchant, ResourceLocation material, boolean tag,
                int baseMaterial, int materialStep, int baseLevels, int levelStep, int baseShelves, int shelfStep) {}

    @Override protected List<Spec> prepare(ResourceManager manager, ProfilerFiller profiler) {
        List<Spec> parsed = new ArrayList<>();
        manager.listResources("enchantment_upgrades", id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (var reader = resource.openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject material = GsonHelper.getAsJsonObject(json, "material");
                boolean tag = material.has("tag");
                var recipeId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    id.getPath().substring("enchantment_upgrades/".length(), id.getPath().length() - 5));
                parsed.add(new Spec(recipeId, ResourceLocation.parse(GsonHelper.getAsString(json, "enchantment")),
                    ResourceLocation.parse(GsonHelper.getAsString(material, tag ? "tag" : "item")), tag,
                    bounded(json, "base_material", 1, 1, 64), bounded(json, "material_step", 1, 0, 64),
                    bounded(json, "base_levels", 3, 0, 1000), bounded(json, "level_step", 3, 0, 1000),
                    bounded(json, "base_shelves", 0, 0, 15), bounded(json, "shelf_step", 3, 0, 15)));
            } catch (Exception e) {
                LoggerFactory.getLogger("EnchantUpgrades").error("Invalid enchantment upgrade {}", id, e);
            }
        });
        return parsed.stream().sorted(Comparator.comparing(r -> r.id.toString())).toList();
    }
    private static int bounded(JsonObject json, String key, int fallback, int min, int max) {
        int value = GsonHelper.getAsInt(json, key, fallback);
        if (value < min || value > max) throw new IllegalArgumentException(key + " must be in [" + min + ", " + max + "]");
        return value;
    }
    @Override protected void apply(List<Spec> parsed, ResourceManager manager, ProfilerFiller profiler) {
        recipes = List.copyOf(parsed);
        revision++;
    }
    public static UpgradeData snapshot(Level level) {
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<UpgradeRecipe> resolved = new ArrayList<>();
        for (Spec spec : recipes) {
            var enchant = registry.get(ResourceKey.create(Registries.ENCHANTMENT, spec.enchant));
            if (enchant.isEmpty()) continue;
            int max = enchant.get().value().getMaxLevel();
            if (spec.baseMaterial + (long) spec.materialStep * (max - 1) > 64 ||
                spec.baseLevels + (long) spec.levelStep * max * (max - 1) / 2 > 32767) continue;
            List<ItemStack> materials = new ArrayList<>();
            if (spec.tag) {
                level.registryAccess().lookupOrThrow(Registries.ITEM).get(TagKey.create(Registries.ITEM, spec.material)).ifPresent(
                    items -> items.forEach(item -> materials.add(new ItemStack(item.value()))));
            } else {
                BuiltInRegistries.ITEM.getOptional(spec.material).ifPresent(item -> materials.add(new ItemStack(item)));
            }
            materials.removeIf(ItemStack::isEmpty);
            if (!materials.isEmpty()) resolved.add(new UpgradeRecipe(spec.id, enchant.get(), new UpgradeMaterial(materials),
                spec.baseMaterial, spec.materialStep, spec.baseLevels, spec.levelStep, spec.baseShelves, spec.shelfStep));
        }
        return new UpgradeData(resolved, revision);
    }
}
