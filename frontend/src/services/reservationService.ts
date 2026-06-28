import apiClient from '@/api/client';
import { Reservation, CreateReservationData } from '../types/reservation';

export const createReservation = async (data: CreateReservationData): Promise<Reservation> => {
  const response = await apiClient.post<Reservation>('/api/v1/reservations', data);
  return response.data;
};