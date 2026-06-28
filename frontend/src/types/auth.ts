export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  name: string;
  email: string;
  password: string;
  phone: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  id: string;
  name: string;
  email: string;
  roles: string[];
}