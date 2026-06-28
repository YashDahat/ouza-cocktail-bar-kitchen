export interface MenuItem {
  id: string;
  name: string;
  price: number;
}

export interface CartItem {
  id: string;
  name: string;
  price: number;
  quantity: number;
}

export interface OrderItem {
  menuItemName: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  orderItems: OrderItem[];
  totalAmount: number;
  orderStatus: string;
  razorpayOrderId: string;
  createdAt: string;
}

export type CreateOrderData = {
  items: {
    menuItemId: string;
    quantity: number;
  }[];
};