export interface Reservation {
  id: string; // UUID
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  partySize: number;
  reservationTime: string; // ISO 8601 format
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
}

export type CreateReservationData = Omit<Reservation, 'id' | 'status'>;