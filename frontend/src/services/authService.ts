import api from '@/api/client';
import { LoginCredentials, RegisterData, AuthResponse } from '@/types/auth';

export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/login', credentials);
  return response.data;
};

export const register = async (data: RegisterData): Promise<any> => {
  const response = await api.post<any>('/auth/register', data);
  return response.data;
};