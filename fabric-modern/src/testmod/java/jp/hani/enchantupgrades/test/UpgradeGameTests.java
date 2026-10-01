package jp.hani.enchantupgrades.test;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class UpgradeGameTests {
    @GameTest(template = "enchant_upgrades:empty") public void upgradeCostsAndPreservesItem(GameTestHelper h) { UpgradeAssertions.upgradeCostsAndPreservesItem(h); }
    @GameTest(template = "enchant_upgrades:empty") public void insufficientResourcesAreAtomic(GameTestHelper h) { UpgradeAssertions.insufficientResourcesAreAtomic(h); }
    @GameTest(template = "enchant_upgrades:empty") public void conflictsAndMaxLevel(GameTestHelper h) { UpgradeAssertions.conflictsAndMaxLevel(h); }
    @GameTest(template = "enchant_upgrades:empty") public void shelvesCheckedAgainOnClick(GameTestHelper h) { UpgradeAssertions.shelvesCheckedAgainOnClick(h); }
    @GameTest(template = "enchant_upgrades:empty") public void enchantedBooksStoreAndUpgrade(GameTestHelper h) { UpgradeAssertions.enchantedBooksStoreAndUpgrade(h); }
    @GameTest(template = "enchant_upgrades:empty") public void invalidRequestsAndDistance(GameTestHelper h) { UpgradeAssertions.invalidRequestsAndDistance(h); }
    @GameTest(template = "enchant_upgrades:empty") public void shiftClickAndCloseReturnItems(GameTestHelper h) { UpgradeAssertions.shiftClickAndCloseReturnItems(h); }
    @GameTest(template = "enchant_upgrades:empty") public void everyRecipeRoundTrips(GameTestHelper h) { UpgradeAssertions.everyRecipeRoundTrips(h); }
    @GameTest(template = "enchant_upgrades:empty") public void maceEnchantsAndConflicts(GameTestHelper h) { UpgradeAssertions.maceEnchantsAndConflicts(h); }
    @GameTest(template = "enchant_upgrades:empty") public void staleDataRejectsWithoutConsumption(GameTestHelper h) { UpgradeAssertions.staleDataRejectsWithoutConsumption(h); }
}
