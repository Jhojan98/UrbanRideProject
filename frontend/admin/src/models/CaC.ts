export default interface CaC {
  idCaC: number;
  description: string;
  status: string;
  idTravel?: number | null;
  type?: string;
  date?: Date | string | null;
}
