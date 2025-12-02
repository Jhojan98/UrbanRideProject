export interface Travel{
  idTravel: number;
  idBicycle: number;
  startStation: string;
  endStation: string;
  startTimestamp: Date;
  endTimestamp: Date | null;
  status: string;
}
