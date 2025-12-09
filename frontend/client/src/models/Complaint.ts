export type ComplaintType = "BICYCLE" | "SLOT" | "STATION";
export type ComplaintStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED" | "PENDING";

export interface ComplaintPayload {
  description: string;
  type: ComplaintType;
  travelId?: number;
  status?: ComplaintStatus;
}

export interface ComplaintResponse {
  k_id_complaints_and_claims: number;
  d_description: string;
  t_status: ComplaintStatus;
  k_id_travel: number | null;
  t_type: ComplaintType;
}
