export interface MenuItemCategory {
  id: string; // UUID
  name: string;
}

export interface MenuItem {
  id: string; // UUID
  name: string;
  description: string;
  price: number;
  categoryName: string;
  imageUrl: string;
  isAvailable: boolean;
}

export type CreateMenuItemData = {
  name: string;
  description: string;
  price: number;
  categoryId: string; // UUID of the category
  imageUrl?: string;
};