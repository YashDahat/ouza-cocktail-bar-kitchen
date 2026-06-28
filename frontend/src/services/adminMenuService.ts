import api from '@/api/client';
import { MenuItem, CreateMenuItemData } from '@/types/menu';

export const getMenuItems = async (): Promise<MenuItem[]> => {
  const response = await api.get<MenuItem[]>('/admin/menu');
  return response.data;
};

export const createMenuItem = async (data: CreateMenuItemData): Promise<MenuItem> => {
  const response = await api.post<MenuItem>('/admin/menu', data);
  return response.data;
};

export const updateMenuItem = async (id: string, data: Partial<CreateMenuItemData>): Promise<MenuItem> => {
  const response = await api.put<MenuItem>(`/admin/menu/${id}`, data);
  return response.data;
};

export const deleteMenuItem = async (id: string): Promise<void> => {
  await api.delete(`/admin/menu/${id}`);
};