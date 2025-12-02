export interface User{
  idUser: number;
  username: string;
  subscription: string | 'NONE';
  email: string;
  timestamp: Date;
}

