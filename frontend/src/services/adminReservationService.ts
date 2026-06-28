import api from '@/api/client';
import { Reservation } from '@/types/reservation';

export const getReservations = async (): Promise<Reservation[]> => {
  const response = await api.get<Reservation[]>('/admin/reservations');
  return response.data;
};

export const updateReservationStatus = async (id: string, status: string): Promise<Reservation> => {
  const response = await api.put<Reservation>(`/admin/reservations/${id}/status`, { status });
  return response.data;
};