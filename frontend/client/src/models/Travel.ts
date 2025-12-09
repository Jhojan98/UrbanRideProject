export default interface Travel{
  idTravel: number;
  requiredAt: Date;
  startedAt: Date;
  endedAt: Date | null;
  status: string;
  uid: string;
  fromIdStation: number;
  toIdStation: number | null;
  travelType: string;

}
