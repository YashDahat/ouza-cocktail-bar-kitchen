import apiClient from '../api/client';
import { MenuItem, MenuItemCategory } from '../types/menu';

export async function getMenu(): Promise<MenuItem[]> {
  const response = await apiClient.get<MenuItem[]>('/menu');
  return response.data;
}

export async function getCategories(): Promise<MenuItemCategory[]> {
  const response = await apiClient.get<MenuItemCategory[]>('/menu/categories');
  return response.data;
}