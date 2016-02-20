package org.inventivetalent.mapmanager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MapManager {

	//If vanilla maps should be allowed to be sent to the players (less efficient, since we need to check the id of every sent map)
	public static boolean ALLOW_VANILLA = false;

	protected static final Set<Short>      OCCUPIED_IDS = new HashSet<>();
	protected static final Set<MapWrapper> MANAGED_MAPS = new HashSet<>();

	public static MapWrapper wrapImage(ArrayImage image) {
		MapWrapper wrapper = new MapWrapper(image);
		MANAGED_MAPS.add(wrapper);
		return wrapper;
	}

	public static void unwrapImage(MapWrapper wrapper) {
		wrapper.getController().clearViewers();
		MANAGED_MAPS.remove(wrapper);
	}

	public static Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player) {
		Set<MapWrapper> visible = new HashSet<>();
		for (MapWrapper wrapper : MANAGED_MAPS) {
			if (wrapper.getController().isViewing(player)) {
				visible.add(wrapper);
			}
		}
		return visible;
	}

	public static void registerOccupiedID(short id) {
		if (!OCCUPIED_IDS.contains(id)) { OCCUPIED_IDS.add(id); }
	}

	public static void unregisterOccupiedID(short id) {
		OCCUPIED_IDS.remove(id);
	}

	public static Set<Short> getOccupiedIdsFor(OfflinePlayer player) {
		Set<Short> ids = new HashSet<>();
		for (MapWrapper wrapper : MANAGED_MAPS) {
			short s;
			if ((s = wrapper.getController().getMapId(player)) >= 0) {
				ids.add(s);
			}
		}
		return ids;
	}

	public static boolean isIdUsedBy(OfflinePlayer player, short id) {
		return getOccupiedIdsFor(player).contains(id);
	}

	public static short getNextFreeIdFor(Player player) throws MapLimitExceededException {
		Set<Short> occupied = getOccupiedIdsFor(player);
		//Add the 'default' occupied IDs
		occupied.addAll(OCCUPIED_IDS);

		int largest = 0;
		for (Short s : occupied) {
			if (s > largest) { largest = s; }
		}

		//Simply increase the maximum id if it's still small enough
		if (largest + 1 < Short.MAX_VALUE) { return (short) (largest + 1); }

		//Otherwise iterate through all options until there is an unused id
		for (short s = 0; s < Short.MAX_VALUE; s++) {
			if (!occupied.contains(s)) {
				return s;
			}
		}

		//If we end up here, this player has no more free ids. Let's hope nobody uses this many Maps.
		throw new MapLimitExceededException("'" + player + "' reached the maximum amount of available Map-IDs");
	}

	public static void clearAllMapsFor(OfflinePlayer player) {
		for (MapWrapper wrapper : getMapsVisibleTo(player)) {
			wrapper.getController().removeViewer(player);
		}
	}

}
