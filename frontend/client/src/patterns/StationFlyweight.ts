import * as L from 'leaflet';
import { type Station, SlotStatus } from '@/models/Station';

// Shared icons (intrinsic state)
class StationFlyweight {
  private static readonly high: L.DivIcon = new L.DivIcon({
    html: `<div style="width:36px;height:36px;background:#4caf50;border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35)"><i class="fa fa-bicycle"></i></div>`,
    className: 'station-high', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });
  private static readonly medium: L.DivIcon = new L.DivIcon({
    html: `<div style="width:36px;height:36px;background:#ff9800;border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35)"><i class="fa fa-bicycle"></i></div>`,
    className: 'station-medium', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });
  private static readonly low: L.DivIcon = new L.DivIcon({
    html: `<div style="width:36px;height:36px;background:#f44336;border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35);animation:pulse 1.8s infinite"><i class="fa fa-bicycle"></i></div><style>@keyframes pulse{0%,100%{transform:scale(1)}50%{transform:scale(1.12)}}</style>`,
    className: 'station-low', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });
  private static readonly none: L.DivIcon = new L.DivIcon({
    html: `<div style="width:36px;height:36px;background:#757575;border:3px solid #eee;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#e0e0e0;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.25)"><i class="fa fa-bicycle"></i></div>`,
    className: 'station-none', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
  });

  // Now accepts optional type ('metro'|'bike') to change the icon
  static icon(available: number, total: number, type?: string): L.DivIcon {
    // Choose base set by percentage
    let base: L.DivIcon
    if (available === 0) base = this.none;
    else {
      const pct = (available/total)*100;
      if (pct > 50) base = this.high;
      else if (pct > 20) base = this.medium;
      else base = this.low;
    }
    // If it's a metro station, replace innerHTML with metro icon
    if (type === 'metro') {
      // Clone but with metro icon — only if html is string
      if (typeof base.options.html === 'string') {
        const replaced = base.options.html.replace('fa-bicycle', 'fa-subway')
        return new L.DivIcon({
          html: replaced,
          className: base.options.className,
          iconSize: base.options.iconSize,
          iconAnchor: base.options.iconAnchor,
          popupAnchor: base.options.popupAnchor
        })
      }
      // If not string, return original base
      return base
    }
    return base;
  }
}

export class StationMarker {
  private marker: L.Marker | null = null;
  private clickHandler: ((station: Station)=>void) | null = null;
  private t: ((key: string, params?: any) => string) | null = null;

  constructor(private station: Station) {}

  setTranslator(t: (key: string, params?: any) => string) {
    this.t = t;
  }

  // Update popup content when language changes
  updatePopupContent(): void {
    if (this.marker) {
      this.marker.setPopupContent(this.popupHtml());
    }
  }

  render(map: L.Map): L.Marker {
    const pos: L.LatLngExpression = [this.station.latitude, this.station.longitude];
    if (!this.marker) {
      this.marker = new L.Marker(pos, { icon: StationFlyweight.icon(this.station.availableSlots, this.station.totalSlots, (this.station as any).type) });
      this.marker.addTo(map);
      // show popup on hover for quick info
      this.marker.on('mouseover', () => { try { this.marker?.openPopup() } catch(e){ void e } });
      this.marker.on('mouseout', () => { try { this.marker?.closePopup() } catch(e){ void e } });
      // preserve click handler capability but do not emit selection by default
      this.marker.on('click', () => { if (this.clickHandler) this.clickHandler(this.station); });
    } else {
      this.marker.setLatLng(pos);
      this.marker.setIcon(StationFlyweight.icon(this.station.availableSlots, this.station.totalSlots, (this.station as any).type));
    }
    this.marker.bindPopup(this.popupHtml());
    return this.marker;
  }

  update(station: Station) {
    this.station = station;
    // Keep icon and popup in sync with latest data (used by telemetry updates)
    if (this.marker) {
      this.marker.setIcon(
        StationFlyweight.icon(
          this.station.availableSlots ?? 0,
          this.station.totalSlots ?? 0,
          (this.station as any).type
        )
      );
      this.updatePopupContent();
    }
  }
  getId(): number { return this.station.idStation; }
  getStation(): Station { return this.station; }
  remove() { if (this.marker) { this.marker.remove(); this.marker = null; } }

  onClick(handler: (station: Station)=>void) {
    this.clickHandler = handler;
    if (this.marker) {
      this.marker.off('click');
      this.marker.on('click', () => { if (this.clickHandler) this.clickHandler(this.station); });
    }
  }

  private popupHtml(): string {
    const s = this.station;
    const pct = ((s.availableSlots/s.totalSlots)*100).toFixed(0);
    const color = s.availableSlots===0? '#757575': parseInt(pct)>50? '#4caf50': parseInt(pct)>20? '#ff9800': '#f44336';

    // Use i18n if available, otherwise fallback to hardcoded strings
    const t = this.t || ((key: string) => key);

    // Show mechanical and electric bikes for any station type (metro or bike)
    const mechanical = (s as any).mechanical ?? 0;
    const electric = (s as any).electric ?? 0;
    const bikeTypesHtml = `
      <div style='margin-top:8px;padding:8px;background:#f5f5f5;border-radius:4px'>
        <div style='font-weight:bold;margin-bottom:6px;color:#374151'>${t('reservation.map.popup.availableTypes')}</div>
        <div style='display:flex;gap:12px'>
          <div><strong>⚙️ ${t('reservation.map.popup.mechanical')}:</strong> <span style='color:#2563eb;font-weight:bold'>${mechanical}</span></div>
          <div><strong>⚡ ${t('reservation.map.popup.electric')}:</strong> <span style='color:#16a34a;font-weight:bold'>${electric}</span></div>
        </div>
      </div>
    `;

    return `<div style='font-family:Arial;min-width:240px'>
      <h4 style='margin:0 0 6px 0;color:#2c3e50;border-bottom:2px solid ${color};padding-bottom:4px'>${(s as any).type === 'metro' ? '🚇' : '🅿️'} ${s.nameStation}</h4>
      <div style='font-size:12px;color:#444;margin-bottom:8px'>
        <strong>${t('reservation.map.popup.available')}:</strong> <span style='color:${color};font-weight:bold'>${s.availableSlots}</span>/<span>${s.totalSlots}</span>
        <span style='background:${color};color:#fff;padding:2px 6px;border-radius:4px;font-size:11px;margin-left:6px'>${pct}%</span>
        <div style='margin-top:4px;font-size:10px;color:#666'>Lat: ${s.latitude.toFixed(5)} | Lon: ${s.longitude.toFixed(5)}</div>
      </div>
      ${bikeTypesHtml}
      ${this.slotsHtml()}
    </div>`;
  }

  private slotsHtml(): string {
    const slots = this.station.slots;
    const t = this.t || ((key: string) => key);

    if (!slots || slots.length===0) return `<em style='color:#888;font-size:11px'>${t('reservation.map.popup.noSlots')}</em>`;
    const sorted = [...slots].sort((a,b)=>a.slotNumber-b.slotNumber);
    const cells = sorted.map(sl=>{
      let bg='#999', icon='?', title=t('reservation.map.popup.slotUnknown');
      switch(sl.status){
        case SlotStatus.AVAILABLE:
          bg='#4caf50'; icon='✓';
          title=t('reservation.map.popup.slotFree', { num: sl.slotNumber });
          break;
        case SlotStatus.OCCUPIED:
          bg='#f44336'; icon='🚲';
          title=t('reservation.map.popup.slotOccupied', { num: sl.slotNumber });
          break;
        case SlotStatus.MAINTENANCE:
          bg='#ff9800'; icon='🔧';
          title=t('reservation.map.popup.slotMaintenance', { num: sl.slotNumber });
          break;
        case SlotStatus.OUT_OF_SERVICE:
          bg='#757575'; icon='✖';
          title=t('reservation.map.popup.slotOutOfService', { num: sl.slotNumber });
          break;
      }
      return `<div title='${title}' style='width:26px;height:26px;background:${bg};color:#fff;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:bold'>${icon}</div>`;
    }).join('');
    return `<div style='margin-top:8px'>
      <strong style='font-size:12px;color:#555;display:block;margin-bottom:4px'>${t('reservation.map.popup.slots')}</strong>
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
  // Update all popup contents when language changes
  updateAllPopups(): void {
    this.pool.forEach(marker => marker.updatePopupContent());
  }
}
