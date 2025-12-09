export default interface Fine{
  k_user_fine: number;
  n_reason: string;
  t_state: string;
  v_amount_snapshot: number;
  f_assigned_at: string | Date;
  k_uid_user?: string;
  fine?: {
    k_id_fine: number;
    d_description: string;
    v_amount: number;
  };
  // Campos legacy para compatibilidad
  idFine?: number;
  reason?: string;
  state?: string;
  amount?: number;
  timestamp?: Date;
}
