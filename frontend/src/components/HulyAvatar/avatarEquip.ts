import type { InventoryItemResponse } from "../../api/store";
import type { AvatarEquippedItem } from "./HulyAvatar";

export function getEquippedAvatarItems(
  inventory: InventoryItemResponse[],
): AvatarEquippedItem[] {
  return inventory
    .filter(
      (item): item is InventoryItemResponse & { assetKey: string } =>
        item.equipped && item.assetKey !== null,
    )
    .map((item) => ({ assetKey: item.assetKey, category: item.category }));
}
