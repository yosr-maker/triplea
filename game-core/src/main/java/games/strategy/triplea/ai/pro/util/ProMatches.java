package games.strategy.triplea.ai.pro.util;

import java.util.List;
import java.util.function.Predicate;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerId;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.engine.data.UnitType;
import games.strategy.triplea.Properties;
import games.strategy.triplea.TripleAUnit;
import games.strategy.triplea.attachments.UnitAttachment;
import games.strategy.triplea.delegate.AbstractMoveDelegate;
import games.strategy.triplea.delegate.Matches;
import games.strategy.triplea.delegate.TerritoryEffectHelper;

/**
 * Pro AI matches.
 */
public final class ProMatches {
  private ProMatches() {}

  public static Predicate<Territory> territoryCanLandAirUnits(final PlayerId player, final GameData data,
      final boolean isCombatMove, final List<Territory> enemyTerritories, final List<Territory> alliedTerritories) {
    Predicate<Territory> match = Matches.airCanLandOnThisAlliedNonConqueredLandTerritory(player, data)
        .and(Matches.territoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, false,
            false, true, true))
        .and(Matches.territoryIsInList(enemyTerritories).negate());
    if (!isCombatMove) {
      match = match.and(Matches.territoryIsNeutralButNotWater()
          .or(Matches.isTerritoryEnemyAndNotUnownedWaterOrImpassableOrRestricted(player, data)).negate());
    }
    return Matches.territoryIsInList(alliedTerritories).or(match);
  }

  public static Predicate<Territory> territoryCanMoveAirUnits(final PlayerId player, final GameData data,
      final boolean isCombatMove) {
    return Matches.territoryDoesNotCostMoneyToEnter(data)
        .and(Matches.territoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, false, false,
            true, false));
  }

  public static Predicate<Territory> territoryCanPotentiallyMoveAirUnits(final PlayerId player, final GameData data) {
    return Matches.territoryDoesNotCostMoneyToEnter(data)
        .and(Matches.territoryIsPassableAndNotRestricted(player, data));
  }

  public static Predicate<Territory> territoryCanMoveAirUnitsAndNoAa(final PlayerId player, final GameData data,
      final boolean isCombatMove) {
    return ProMatches.territoryCanMoveAirUnits(player, data, isCombatMove)
        .and(Matches.territoryHasEnemyAaForFlyOver(player, data).negate());
  }

  public static Predicate<Territory> territoryCanMoveSpecificLandUnit(final PlayerId player, final GameData data,
      final boolean isCombatMove, final Unit u) {
    return t -> {
      final Predicate<Territory> territoryMatch = Matches.territoryDoesNotCostMoneyToEnter(data)
          .and(Matches.territoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, true, false,
              false, false));
      final Predicate<Unit> unitMatch =
          Matches.unitIsOfTypes(TerritoryEffectHelper.getUnitTypesForUnitsNotAllowedIntoTerritory(t)).negate();
      return territoryMatch.test(t) && unitMatch.test(u);
    };
  }

  public static Predicate<Territory> territoryCanPotentiallyMoveSpecificLandUnit(final PlayerId player,
      final GameData data,
      final Unit u) {
    return t -> {
      final Predicate<Territory> territoryMatch = Matches.territoryDoesNotCostMoneyToEnter(data)
          .and(Matches.territoryIsPassableAndNotRestricted(player, data));
      final Predicate<Unit> unitMatch =
          Matches.unitIsOfTypes(TerritoryEffectHelper.getUnitTypesForUnitsNotAllowedIntoTerritory(t)).negate();
      return territoryMatch.test(t) && unitMatch.test(u);
    };
  }

  public static Predicate<Territory> territoryCanMoveLandUnits(final PlayerId player, final GameData data,
      final boolean isCombatMove) {
    return Matches.territoryDoesNotCostMoneyToEnter(data)
        .and(Matches.territoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, true, false,
            false, false));
  }

  public static Predicate<Territory> territoryCanPotentiallyMoveLandUnits(final PlayerId player, final GameData data) {
    return Matches.territoryIsLand()
        .and(Matches.territoryDoesNotCostMoneyToEnter(data))
        .and(Matches.territoryIsPassableAndNotRestricted(player, data));
  }

  public static Predicate<Territory> territoryCanMoveLandUnitsAndIsAllied(final PlayerId player, final GameData data) {
    return Matches.isTerritoryAllied(player, data).and(territoryCanMoveLandUnits(player, data, false));
  }

  public static Predicate<Territory> territoryCanMoveLandUnitsThrough(final PlayerId player, final GameData data,
      final Unit u, final Territory startTerritory, final boolean isCombatMove,
      final List<Territory> enemyTerritories) {
    return t -> {
      if (isCombatMove && Matches.unitCanBlitz().test(u) && TerritoryEffectHelper.unitKeepsBlitz(u, startTerritory)) {
        final Predicate<Territory> alliedWithNoEnemiesMatch = Matches.isTerritoryAllied(player, data)
            .and(Matches.territoryHasNoEnemyUnits(player, data));
        final Predicate<Territory> alliedOrBlitzableMatch =
            alliedWithNoEnemiesMatch.or(territoryIsBlitzable(player, data, u));
        return ProMatches.territoryCanMoveSpecificLandUnit(player, data, isCombatMove, u)
            .and(alliedOrBlitzableMatch)
            .and(Matches.territoryIsInList(enemyTerritories).negate())
            .test(t);
      }
      return ProMatches.territoryCanMoveSpecificLandUnit(player, data, isCombatMove, u)
          .and(Matches.isTerritoryAllied(player, data))
          .and(Matches.territoryHasNoEnemyUnits(player, data))
          .and(Matches.territoryIsInList(enemyTerritories).negate())
          .test(t);
    };
  }

  public static Predicate<Territory> territoryCanMoveLandUnitsThroughIgnoreEnemyUnits(final PlayerId player,
      final GameData data, final Unit u, final Territory startTerritory, final boolean isCombatMove,
      final List<Territory> blockedTerritories, final List<Territory> clearedTerritories) {
    Predicate<Territory> alliedMatch = Matches.isTerritoryAllied(player, data)
        .or(Matches.territoryIsInList(clearedTerritories));
    if (isCombatMove && Matches.unitCanBlitz().test(u) && TerritoryEffectHelper.unitKeepsBlitz(u, startTerritory)) {
      alliedMatch = Matches.isTerritoryAllied(player, data)
          .or(Matches.territoryIsInList(clearedTerritories))
          .or(territoryIsBlitzable(player, data, u));
    }
    return ProMatches.territoryCanMoveSpecificLandUnit(player, data, isCombatMove, u)
        .and(alliedMatch)
        .and(Matches.territoryIsInList(blockedTerritories).negate());
  }

  private static Predicate<Territory> territoryIsBlitzable(final PlayerId player, final GameData data, final Unit u) {
    return t -> Matches.territoryIsBlitzable(player, data).test(t) && TerritoryEffectHelper.unitKeepsBlitz(u, t);
  }

  public static Predicate<Territory> territoryCanMoveSeaUnits(final PlayerId player, final GameData data,
      final boolean isCombatMove) {
    return t -> {
      final boolean navalMayNotNonComIntoControlled =
          Properties.getWW2V2(data) || Properties.getNavalUnitsMayNotNonCombatMoveIntoControlledSeaZones(data);
      if (!isCombatMove && navalMayNotNonComIntoControlled
          && Matches.isTerritoryEnemyAndNotUnownedWater(player, data).test(t)) {
        return false;
      }
      final Predicate<Territory> match = Matches.territoryDoesNotCostMoneyToEnter(data)
          .and(Matches.territoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, false, true,
              false, false));
      return match.test(t);
    };
  }

  public static Predicate<Territory> territoryCanMoveSeaUnitsThrough(final PlayerId player, final GameData data,
      final boolean isCombatMove) {
    return territoryCanMoveSeaUnits(player, data, isCombatMove).and(territoryHasOnlyIgnoredUnits(player, data));
  }

  public static Predicate<Territory> territoryCanMoveSeaUnitsAndNotInList(final PlayerId player, final GameData data,
      final boolean isCombatMove, final List<Territory> notTerritories) {
    return territoryCanMoveSeaUnits(player, data, isCombatMove).and(Matches.territoryIsNotInList(notTerritories));
  }

  public static Predicate<Territory> territoryCanMoveSeaUnitsThroughOrClearedAndNotInList(final PlayerId player,
      final GameData data, final boolean isCombatMove, final List<Territory> clearedTerritories,
      final List<Territory> notTerritories) {
    final Predicate<Territory> onlyIgnoredOrClearedMatch = territoryHasOnlyIgnoredUnits(player, data)
        .or(Matches.territoryIsInList(clearedTerritories));
    return territoryCanMoveSeaUnits(player, data, isCombatMove)
        .and(onlyIgnoredOrClearedMatch)
        .and(Matches.territoryIsNotInList(notTerritories));
  }

  private static Predicate<Territory> territoryHasOnlyIgnoredUnits(final PlayerId player, final GameData data) {
    return t -> {
      final Predicate<Unit> subOnly = Matches.unitIsInfrastructure()
          .or(Matches.unitCanBeMovedThroughByEnemies())
          .or(Matches.enemyUnit(player, data).negate());
      return t.getUnitCollection().allMatch(subOnly) || Matches.territoryHasNoEnemyUnits(player, data).test(t);
    };
  }

  public static Predicate<Territory> territoryHasEnemyUnitsOrCantBeHeld(final PlayerId player, final GameData data,
      final List<Territory> territoriesThatCantBeHeld) {
    return Matches.territoryHasEnemyUnits(player, data).or(Matches.territoryIsInList(territoriesThatCantBeHeld));
  }

  public static Predicate<Territory> territoryHasPotentialEnemyUnits(final PlayerId player, final GameData data,
      final List<PlayerId> players) {
    return Matches.territoryHasEnemyUnits(player, data)
        .or(Matches.territoryHasUnitsThatMatch(Matches.unitOwnedBy(players)));
  }

  public static Predicate<Territory> territoryHasNoEnemyUnitsOrCleared(final PlayerId player, final GameData data,
      final List<Territory> clearedTerritories) {
    return Matches.territoryHasNoEnemyUnits(player, data).or(Matches.territoryIsInList(clearedTerritories));
  }

  public static Predicate<Territory> territoryIsEnemyOrHasEnemyUnitsOrCantBeHeld(final PlayerId player,
      final GameData data, final List<Territory> territoriesThatCantBeHeld) {
    return Matches.isTerritoryEnemyAndNotUnownedWater(player, data)
        .or(Matches.territoryHasEnemyUnits(player, data))
        .or(Matches.territoryIsInList(territoriesThatCantBeHeld));
  }

  public static Predicate<Territory> territoryHasInfraFactoryAndIsLand() {
    final Predicate<Unit> infraFactory = Matches.unitCanProduceUnits().and(Matches.unitIsInfrastructure());
    return Matches.territoryIsLand().and(Matches.territoryHasUnitsThatMatch(infraFactory));
  }

  public static Predicate<Territory> territoryHasInfraFactoryAndIsEnemyLand(final PlayerId player,
      final GameData data) {
    return territoryHasInfraFactoryAndIsLand().and(Matches.isTerritoryEnemy(player, data));
  }

  static Predicate<Territory> territoryHasInfraFactoryAndIsOwnedByPlayersOrCantBeHeld(final PlayerId player,
      final List<PlayerId> players, final List<Territory> territoriesThatCantBeHeld) {
    final Predicate<Territory> ownedAndCantBeHeld = Matches.isTerritoryOwnedBy(player)
        .and(Matches.territoryIsInList(territoriesThatCantBeHeld));
    final Predicate<Territory> enemyOrOwnedCantBeHeld = Matches.isTerritoryOwnedBy(players).or(ownedAndCantBeHeld);
    return territoryHasInfraFactoryAndIsLand().and(enemyOrOwnedCantBeHeld);
  }

  public static Predicate<Territory> territoryHasFactoryAndIsNotConqueredOwnedLand(final PlayerId player,
      final GameData data) {
    return territoryIsNotConqueredOwnedLand(player, data).and(territoryHasFactoryAndIsOwnedLand(player));
  }

  private static Predicate<Territory> territoryHasFactoryAndIsOwnedLand(final PlayerId player) {
    final Predicate<Unit> factoryMatch = Matches.unitIsOwnedBy(player)
        .and(Matches.unitCanProduceUnits());
    return Matches.isTerritoryOwnedBy(player)
        .and(Matches.territoryIsLand())
        .and(Matches.territoryHasUnitsThatMatch(factoryMatch));
  }

  public static Predicate<Territory> territoryHasNonMobileFactoryAndIsNotConqueredOwnedLand(final PlayerId player,
      final GameData data) {
    return territoryHasNonMobileInfraFactory().and(territoryHasFactoryAndIsNotConqueredOwnedLand(player, data));
  }

  private static Predicate<Territory> territoryHasNonMobileInfraFactory() {
    final Predicate<Unit> nonMobileInfraFactoryMatch = Matches.unitCanProduceUnits()
        .and(Matches.unitIsInfrastructure())
        .and(Matches.unitHasMovementLeft().negate());
    return Matches.territoryHasUnitsThatMatch(nonMobileInfraFactoryMatch);
  }

  public static Predicate<Territory> territoryHasInfraFactoryAndIsOwnedLand(final PlayerId player) {
    final Predicate<Unit> infraFactoryMatch = Matches.unitIsOwnedBy(player)
        .and(Matches.unitCanProduceUnits())
        .and(Matches.unitIsInfrastructure());
    return Matches.isTerritoryOwnedBy(player)
        .and(Matches.territoryIsLand())
        .and(Matches.territoryHasUnitsThatMatch(infraFactoryMatch));
  }

  public static Predicate<Territory> territoryHasInfraFactoryAndIsAlliedLand(final PlayerId player,
      final GameData data) {
    final Predicate<Unit> infraFactoryMatch = Matches.unitCanProduceUnits().and(Matches.unitIsInfrastructure());
    return Matches.isTerritoryAllied(player, data)
        .and(Matches.territoryIsLand())
        .and(Matches.territoryHasUnitsThatMatch(infraFactoryMatch));
  }

  public static Predicate<Territory> territoryHasInfraFactoryAndIsOwnedLandAdjacentToSea(final PlayerId player,
      final GameData data) {
    return territoryHasInfraFactoryAndIsOwnedLand(player)
        .and(Matches.territoryHasNeighborMatching(data, Matches.territoryIsWater()));
  }

  public static Predicate<Territory> territoryHasNoInfraFactoryAndIsNotConqueredOwnedLand(final PlayerId player,
      final GameData data) {
    return territoryIsNotConqueredOwnedLand(player, data)
        .and(territoryHasInfraFactoryAndIsOwnedLand(player).negate());
  }

  public static Predicate<Territory> territoryHasNeighborOwnedByAndHasLandUnit(final GameData data,
      final List<PlayerId> players) {
    final Predicate<Territory> territoryMatch = Matches.isTerritoryOwnedBy(players)
        .and(Matches.territoryHasUnitsThatMatch(Matches.unitIsLand()));
    return Matches.territoryHasNeighborMatching(data, territoryMatch);
  }

  static Predicate<Territory> territoryIsAlliedLandAndHasNoEnemyNeighbors(final PlayerId player, final GameData data) {
    final Predicate<Territory> alliedLand = territoryCanMoveLandUnits(player, data, false)
        .and(Matches.isTerritoryAllied(player, data));
    final Predicate<Territory> hasNoEnemyNeighbors = Matches
        .territoryHasNeighborMatching(data, ProMatches.territoryIsEnemyNotNeutralLand(player, data)).negate();
    return alliedLand.and(hasNoEnemyNeighbors);
  }

  public static Predicate<Territory> territoryIsEnemyLand(final PlayerId player, final GameData data) {
    return territoryCanMoveLandUnits(player, data, false).and(Matches.isTerritoryEnemy(player, data));
  }

  public static Predicate<Territory> territoryIsEnemyNotNeutralLand(final PlayerId player, final GameData data) {
    return territoryIsEnemyLand(player, data).and(Matches.territoryIsNeutralButNotWater().negate())
        .and(t -> !ProUtils.isPassiveNeutralPlayer(t.getOwner()));
  }

  public static Predicate<Territory> territoryIsOrAdjacentToEnemyNotNeutralLand(final PlayerId player,
      final GameData data) {
    final Predicate<Territory> isMatch = territoryIsEnemyLand(player, data)
        .and(Matches.territoryIsNeutralButNotWater().negate()).and(t -> !ProUtils.isPassiveNeutralPlayer(t.getOwner()));
    final Predicate<Territory> adjacentMatch = territoryCanMoveLandUnits(player, data, false)
        .and(Matches.territoryHasNeighborMatching(data, isMatch));
    return isMatch.or(adjacentMatch);
  }

  public static Predicate<Territory> territoryIsEnemyNotNeutralOrAllied(final PlayerId player, final GameData data) {
    return territoryIsEnemyNotNeutralLand(player, data)
        .or(Matches.territoryIsLand().and(Matches.isTerritoryAllied(player, data)));
  }

  public static Predicate<Territory> territoryIsEnemyOrCantBeHeld(final PlayerId player, final GameData data,
      final List<Territory> territoriesThatCantBeHeld) {
    return Matches.isTerritoryEnemyAndNotUnownedWater(player, data)
        .or(Matches.territoryIsInList(territoriesThatCantBeHeld));
  }

  public static Predicate<Territory> territoryIsPotentialEnemy(final PlayerId player, final GameData data,
      final List<PlayerId> players) {
    return Matches.isTerritoryEnemyAndNotUnownedWater(player, data).or(Matches.isTerritoryOwnedBy(players));
  }

  public static Predicate<Territory> territoryIsPotentialEnemyOrHasPotentialEnemyUnits(final PlayerId player,
      final GameData data, final List<PlayerId> players) {
    return territoryIsPotentialEnemy(player, data, players).or(territoryHasPotentialEnemyUnits(player, data, players));
  }

  public static Predicate<Territory> territoryIsEnemyOrCantBeHeldAndIsAdjacentToMyLandUnits(final PlayerId player,
      final GameData data, final List<Territory> territoriesThatCantBeHeld) {
    final Predicate<Unit> myUnitIsLand = Matches.unitIsOwnedBy(player).and(Matches.unitIsLand());
    final Predicate<Territory> territoryIsLandAndAdjacentToMyLandUnits = Matches.territoryIsLand()
        .and(Matches.territoryHasNeighborMatching(data, Matches.territoryHasUnitsThatMatch(myUnitIsLand)));
    return territoryIsLandAndAdjacentToMyLandUnits
        .and(territoryIsEnemyOrCantBeHeld(player, data, territoriesThatCantBeHeld));
  }

  public static Predicate<Territory> territoryIsNotConqueredOwnedLand(final PlayerId player, final GameData data) {
    return t -> !AbstractMoveDelegate.getBattleTracker(data).wasConquered(t)
        && Matches.isTerritoryOwnedBy(player).and(Matches.territoryIsLand()).test(t);
  }

  public static Predicate<Territory> territoryIsWaterAndAdjacentToOwnedFactory(final PlayerId player,
      final GameData data) {
    final Predicate<Territory> hasOwnedFactoryNeighbor =
        Matches.territoryHasNeighborMatching(data, ProMatches.territoryHasInfraFactoryAndIsOwnedLand(player));
    return hasOwnedFactoryNeighbor.and(ProMatches.territoryCanMoveSeaUnits(player, data, true));
  }

  private static Predicate<Unit> unitCanBeMovedAndIsOwned(final PlayerId player) {
    return Matches.unitIsOwnedBy(player).and(Matches.unitHasMovementLeft());
  }

  public static Predicate<Unit> unitCanBeMovedAndIsOwnedAir(final PlayerId player, final boolean isCombatMove) {
    return u -> (!isCombatMove || !Matches.unitCanNotMoveDuringCombatMove().test(u))
        && unitCanBeMovedAndIsOwned(player).and(Matches.unitIsAir()).test(u);
  }

  public static Predicate<Unit> unitCanBeMovedAndIsOwnedLand(final PlayerId player, final boolean isCombatMove) {
    return u -> (!isCombatMove || !Matches.unitCanNotMoveDuringCombatMove().test(u))
        && unitCanBeMovedAndIsOwned(player)
            .and(Matches.unitIsLand())
            .and(Matches.unitIsBeingTransported().negate())
            .test(u);

  }

  public static Predicate<Unit> unitCanBeMovedAndIsOwnedSea(final PlayerId player, final boolean isCombatMove) {
    return u -> (!isCombatMove || !Matches.unitCanNotMoveDuringCombatMove().test(u))
        && unitCanBeMovedAndIsOwned(player).and(Matches.unitIsSea()).test(u);

  }

  public static Predicate<Unit> unitCanBeMovedAndIsOwnedTransport(final PlayerId player, final boolean isCombatMove) {
    return u -> (!isCombatMove || !Matches.unitCanNotMoveDuringCombatMove().test(u)) && unitCanBeMovedAndIsOwned(player)
        .and(Matches.unitIsTransport()).test(u);

  }

  public static Predicate<Unit> unitCanBeMovedAndIsOwnedBombard(final PlayerId player) {
    return u -> !Matches.unitCanNotMoveDuringCombatMove().test(u)
        && unitCanBeMovedAndIsOwned(player).and(Matches.unitCanBombard(player)).test(u);
  }

  public static Predicate<Unit> unitCanBeMovedAndIsOwnedNonCombatInfra(final PlayerId player) {
    return unitCanBeMovedAndIsOwned(player)
        .and(Matches.unitCanNotMoveDuringCombatMove())
        .and(Matches.unitIsInfrastructure());
  }

  public static Predicate<Unit> unitCantBeMovedAndIsAlliedDefender(final PlayerId player, final GameData data,
      final Territory t) {
    final Predicate<Unit> myUnitHasNoMovementMatch = Matches.unitIsOwnedBy(player)
        .and(Matches.unitHasMovementLeft().negate());
    final Predicate<Unit> alliedUnitMatch = Matches.unitIsOwnedBy(player).negate()
        .and(Matches.isUnitAllied(player, data))
        .and(Matches.unitIsBeingTransportedByOrIsDependentOfSomeUnitInThisList(
            t.getUnits(), player, data, false).negate());
    return myUnitHasNoMovementMatch.or(alliedUnitMatch);
  }

  public static Predicate<Unit> unitCantBeMovedAndIsAlliedDefenderAndNotInfra(final PlayerId player,
      final GameData data,
      final Territory t) {
    return unitCantBeMovedAndIsAlliedDefender(player, data, t).and(Matches.unitIsNotInfrastructure());
  }

  public static Predicate<Unit> unitIsAlliedLandAndNotInfra(final PlayerId player, final GameData data) {
    return Matches.unitIsLand()
        .and(Matches.isUnitAllied(player, data))
        .and(Matches.unitIsNotInfrastructure());
  }

  public static Predicate<Unit> unitIsAlliedNotOwned(final PlayerId player, final GameData data) {
    return Matches.unitIsOwnedBy(player).negate().and(Matches.isUnitAllied(player, data));
  }

  public static Predicate<Unit> unitIsAlliedNotOwnedAir(final PlayerId player, final GameData data) {
    return unitIsAlliedNotOwned(player, data).and(Matches.unitIsAir());
  }

  static Predicate<Unit> unitIsAlliedAir(final PlayerId player, final GameData data) {
    return Matches.isUnitAllied(player, data).and(Matches.unitIsAir());
  }

  public static Predicate<Unit> unitIsEnemyAir(final PlayerId player, final GameData data) {
    return Matches.enemyUnit(player, data).and(Matches.unitIsAir());
  }

  public static Predicate<Unit> unitIsEnemyAndNotInfa(final PlayerId player, final GameData data) {
    return Matches.enemyUnit(player, data).and(Matches.unitIsNotInfrastructure());
  }

  public static Predicate<Unit> unitIsEnemyNotLand(final PlayerId player, final GameData data) {
    return Matches.enemyUnit(player, data).and(Matches.unitIsNotLand());
  }

  static Predicate<Unit> unitIsEnemyNotNeutral(final PlayerId player, final GameData data) {
    return Matches.enemyUnit(player, data).and(unitIsNeutral().negate());
  }

  private static Predicate<Unit> unitIsNeutral() {
    return u -> ProUtils.isNeutralPlayer(u.getOwner());
  }

  static Predicate<Unit> unitIsOwnedAir(final PlayerId player) {
    return Matches.unitOwnedBy(player).and(Matches.unitIsAir());
  }

  public static Predicate<Unit> unitIsOwnedAndMatchesTypeAndIsTransporting(final PlayerId player,
      final UnitType unitType) {
    return Matches.unitIsOwnedBy(player)
        .and(Matches.unitIsOfType(unitType))
        .and(Matches.unitIsTransporting());
  }

  public static Predicate<Unit> unitIsOwnedAndMatchesTypeAndNotTransporting(final PlayerId player,
      final UnitType unitType) {
    return Matches.unitIsOwnedBy(player)
        .and(Matches.unitIsOfType(unitType))
        .and(Matches.unitIsTransporting().negate());
  }

  public static Predicate<Unit> unitIsOwnedCarrier(final PlayerId player) {
    return unit -> UnitAttachment.get(unit.getType()).getCarrierCapacity() != -1
        && Matches.unitIsOwnedBy(player).test(unit);
  }

  public static Predicate<Unit> unitIsOwnedNotLand(final PlayerId player) {
    return Matches.unitIsNotLand().and(Matches.unitIsOwnedBy(player));
  }

  public static Predicate<Unit> unitIsOwnedTransport(final PlayerId player) {
    return Matches.unitIsOwnedBy(player).and(Matches.unitIsTransport());
  }

  public static Predicate<Unit> unitIsOwnedTransportableUnit(final PlayerId player) {
    return Matches.unitIsOwnedBy(player)
        .and(Matches.unitCanBeTransported())
        .and(Matches.unitCanMove());
  }

  public static Predicate<Unit> unitIsOwnedCombatTransportableUnit(final PlayerId player) {
    return unitIsOwnedTransportableUnit(player).and(Matches.unitCanNotMoveDuringCombatMove().negate());
  }

  public static Predicate<Unit> unitIsOwnedTransportableUnitAndCanBeLoaded(final PlayerId player, final Unit transport,
      final boolean isCombatMove) {
    return u -> (!isCombatMove || (!Matches.unitCanNotMoveDuringCombatMove().test(u)
        && UnitAttachment.get(u.getType()).canInvadeFrom(transport)))
        && unitIsOwnedTransportableUnit(player)
            .and(Matches.unitHasNotMoved())
            .and(Matches.unitHasMovementLeft())
            .and(Matches.unitIsBeingTransported().negate())
            .test(u);
  }

  public static Predicate<Unit> unitHasLessMovementThan(final Unit unit) {
    return u -> TripleAUnit.get(u).getMovementLeft() < TripleAUnit.get(unit).getMovementLeft();
  }

}
