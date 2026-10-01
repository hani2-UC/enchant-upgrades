package jp.hani.enchantupgrades.test;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
@GameTestHolder("enchant_upgrades")
@PrefixGameTestTemplate(false)
public final class UpgradeGameTests {
    @GameTest(template = "empty") public static void upgradeCostsAndPreservesItem(GameTestHelper h) { UpgradeAssertions.upgradeCostsAndPreservesItem(h); }
    @GameTest(template = "empty") public static void insufficientResourcesAreAtomic(GameTestHelper h) { UpgradeAssertions.insufficientResourcesAreAtomic(h); }
    @GameTest(template = "empty") public static void conflictsAndMaxLevel(GameTestHelper h) { UpgradeAssertions.conflictsAndMaxLevel(h); }
    @GameTest(template = "empty") public static void shelvesCheckedAgainOnClick(GameTestHelper h) { UpgradeAssertions.shelvesCheckedAgainOnClick(h); }
    @GameTest(template = "empty") public static void enchantedBooksStoreAndUpgrade(GameTestHelper h) { UpgradeAssertions.enchantedBooksStoreAndUpgrade(h); }
    @GameTest(template = "empty") public static void invalidRequestsAndDistance(GameTestHelper h) { UpgradeAssertions.invalidRequestsAndDistance(h); }
    @GameTest(template = "empty") public static void shiftClickAndCloseReturnItems(GameTestHelper h) { UpgradeAssertions.shiftClickAndCloseReturnItems(h); }
    @GameTest(template = "empty") public static void everyRecipeRoundTrips(GameTestHelper h) { UpgradeAssertions.everyRecipeRoundTrips(h); }
    @GameTest(template = "empty") public static void maceEnchantsAndConflicts(GameTestHelper h) { UpgradeAssertions.maceEnchantsAndConflicts(h); }
    @GameTest(template = "empty") public static void staleDataRejectsWithoutConsumption(GameTestHelper h) { UpgradeAssertions.staleDataRejectsWithoutConsumption(h); }
}
