/**
 * Estado de una queja o reclamo
 */
export enum ComplaintStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

/**
 * Tipo de queja o reclamo
 */
export enum ComplaintType {
  COMPLAINT = 'COMPLAINT',
  CLAIM = 'CLAIM',
  SUGGESTION = 'SUGGESTION',
  OTHER = 'OTHER'
}

/**
 * Modelo de Queja/Reclamo
 */
export default interface Complaint {
  k_id_complaints_and_claims: number;
  k_id_travel: number;
  d_description: string;
  t_status: ComplaintStatus | string;
  t_type: ComplaintType | string;
  created_at?: Date | string;
  updated_at?: Date | string;
  user_id?: number;
  user_name?: string;
}
