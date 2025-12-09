export default interface User {
  // IDs posibles según origen de datos

  uidUser: string;
  userName: string;
  subscriptionType?: string;
  email: string;
  timestamp: Date;
  balance: number;
}

