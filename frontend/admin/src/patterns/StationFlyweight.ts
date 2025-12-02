import * as L from 'leaflet';
import { type Station, SlotStatus } from '@/models/Station';

// Íconos compartidos (estado intrínseco)
class StationFlyweight {
  private static readonly high: L.DivIcon = L.divIcon({
    html: `<div style="width:36px;height:36px;background:#4caf50;border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35)">🅿️</div>`,
    className: 'station-high', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });
  private static readonly medium: L.DivIcon = L.divIcon({
    html: `<div style="width:36px;height:36px;background:#ff9800;border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35)">🅿️</div>`,
    className: 'station-medium', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });
  private static readonly low: L.DivIcon = L.divIcon({
    html: `<div style="width:36px;height:36px;background:#f44336;border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35);animation:pulse 1.8s infinite">🅿️</div><style>@keyframes pulse{0%,100%{transform:scale(1)}50%{transform:scale(1.12)}}</style>`,
    className: 'station-low', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });
  private static readonly none: L.DivIcon = L.divIcon({
    html: `<div style="width:36px;height:36px;background:#757575;border:3px solid #eee;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#e0e0e0;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.25)">🅿️</div>`,
    className: 'station-none', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });

  static icon(available: number, total: number): L.DivIcon {
    if (available === 0) return this.none;
    const pct = (available/total)*100;
    if (pct > 50) return this.high;
    if (pct > 20) return this.medium;
    return this.low;
  }
}

export class StationMarker {
  private marker: L.Marker | null = null;
  constructor(private station: Station) {}

  render(map: L.Map): L.Marker {
    const pos: L.LatLngExpression = [this.station.latitude, this.station.longitude];
    if (!this.marker) {
      this.marker = L.marker(pos, { icon: StationFlyweight.icon(this.station.availableSlots, this.station.totalSlots) });
      this.marker.addTo(map);
    } else {
      this.marker.setLatLng(pos);
      this.marker.setIcon(StationFlyweight.icon(this.station.availableSlots, this.station.totalSlots));
    }
    this.marker.bindPopup(this.popupHtml());
    return this.marker;
  }

  update(station: Station) { this.station = station; }
  getId(): number { return this.station.idStation; }
  getStation(): Station { return this.station; }
  remove() { if (this.marker) { this.marker.remove(); this.marker = null; } }

  private popupHtml(): string {
    const s = this.station;
    const pct = ((s.availableSlots/s.totalSlots)*100).toFixed(0);
    const color = s.availableSlots===0? '#757575': parseInt(pct)>50? '#4caf50': parseInt(pct)>20? '#ff9800': '#f44336';
    return `<div style='font-family:Arial;min-width:240px'>
      <h4 style='margin:0 0 6px 0;color:#2c3e50;border-bottom:2px solid ${color};padding-bottom:4px'>🅿️ ${s.nameStation}</h4>
      <div style='font-size:12px;color:#444;margin-bottom:8px'>
        <strong>Disponibles:</strong> <span style='color:${color};font-weight:bold'>${s.availableSlots}</span>/<span>${s.totalSlots}</span>
        <span style='background:${color};color:#fff;padding:2px 6px;border-radius:4px;font-size:11px;margin-left:6px'>${pct}%</span>
        <div style='margin-top:4px'>Lat: ${s.latitude.toFixed(5)} | Lon: ${s.longitude.toFixed(5)}</div>
      </div>
      ${this.slotsHtml()}
    </div>`;
  }

  private slotsHtml(): string {
    const slots = this.station.slots;
    if (!slots || slots.length===0) return `<em style='color:#888;font-size:11px'>Sin slots</em>`;
    const sorted = [...slots].sort((a,b)=>a.slotNumber-b.slotNumber);
    const cells = sorted.map(sl=>{
      let bg='#999', icon='?', title='Desconocido';
      switch(sl.status){
        case SlotStatus.AVAILABLE: bg='#4caf50'; icon='✓'; title=`Slot ${sl.slotNumber}: Libre`; break;
        case SlotStatus.OCCUPIED: bg='#f44336'; icon='🚲'; title=`Slot ${sl.slotNumber}: Ocupado`; break;
        case SlotStatus.MAINTENANCE: bg='#ff9800'; icon='🔧'; title=`Slot ${sl.slotNumber}: Mantto.`; break;
        case SlotStatus.OUT_OF_SERVICE: bg='#757575'; icon='✖'; title=`Slot ${sl.slotNumber}: Fuera`; break;
      }
      return `<div title='${title}' style='width:26px;height:26px;background:${bg};color:#fff;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:bold'>${icon}</div>`;
    }).join('');
    return `<div style='margin-top:8px'>
      <strong style='font-size:12px;color:#555;display:block;margin-bottom:4px'>Slots</strong>
      <div style='display:grid;grid-template-columns:repeat(auto-fill,26px);gap:4px;max-height:120px;overflow:auto;padding:6px;background:#f5f5f5;border-radius:4px'>${cells}</div>
    </div>`;
  }
}

export class StationFactory {
  private pool = new Map<number, StationMarker>();
  getStationMarker(station: Station): StationMarker {
    let mk = this.pool.get(station.idStation);
    if (mk) { mk.update(station); return mk; }
    mk = new StationMarker(station); this.pool.set(station.idStation, mk); return mk;
  }
  removeStationMarker(id: number){ const m=this.pool.get(id); if(m){m.remove(); this.pool.delete(id);} }
  getAllMarkers(): StationMarker[] { return [...this.pool.values()]; }
  clear(){ this.pool.forEach(m=>m.remove()); this.pool.clear(); }
  size(): number { return this.pool.size; }
  getMarkerById(id:number){ return this.pool.get(id); }
}
