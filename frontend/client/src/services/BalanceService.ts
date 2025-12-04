// Frontend/src/services/BalanceService.ts
const BalanceService = {
  async getBalance(uid: string): Promise<number> {
    try {
      // Llama directamente al usuario-service (puerto 8001)
      const response = await fetch(`http://localhost:8001/balance/${uid}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      if (!response.ok) {
        console.error('Error obteniendo saldo:', response.status);
        return 0;
      }
      
      const data = await response.json();
      return data.balance || 0;
    } catch (error) {
      console.error('Error en BalanceService.getBalance:', error);
      return 0;
    }
  },
  
  async addBalance(uid: string, amount: number): Promise<number> {
    try {
      const response = await fetch(`http://localhost:8001/balance/${uid}/add?amount=${amount}`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Error al agregar saldo');
      }
      
      const data = await response.json();
      return data.balance;
    } catch (error) {
      console.error('Error en BalanceService.addBalance:', error);
      throw error;
    }
  },
  
  async subtractBalance(uid: string, amount: number): Promise<number> {
    try {
      const response = await fetch(`http://localhost:8001/balance/${uid}/subtract?amount=${amount}`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Error al restar saldo');
      }
      
      const data = await response.json();
      return data.balance;
    } catch (error) {
      console.error('Error en BalanceService.subtractBalance:', error);
      throw error;
    }
  }
}

export default BalanceService;