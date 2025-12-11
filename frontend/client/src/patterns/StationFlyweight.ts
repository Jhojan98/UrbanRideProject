import * as L from 'leaflet';
import { type Station, SlotStatus } from '@/models/Station';

// Shared icons (intrinsic state)
class StationFlyweight {
  private static createDivIcon(opts: { color: string; icon: string; pulse?: boolean }): L.DivIcon {
    const pulseCss = opts.pulse ? 'animation:pulse 1.8s infinite' : '';
    const html = `<div style="width:32px;height:32px;background:${opts.color};border:2px solid #fff;border-radius:6px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:16px;box-shadow:0 2px 4px rgba(0,0,0,.3);${pulseCss}"><i class="fa ${opts.icon}"></i></div>${opts.pulse ? '<style>@keyframes pulse{0%,100%{transform:scale(1)}50%{transform:scale(1.12)}}</style>' : ''}`;
    return new L.DivIcon({ html, className: 'station-icon', iconSize: [32,32], iconAnchor: [16,32], popupAnchor: [0,-32] });
  }

  static icon(available: number, total: number, type?: string): L.DivIcon {
    const typeStyles: Record<string, { color: string; icon: string }> = {
      metro: { color: '#1e88e5', icon: 'fa-subway' },
      financial: { color: '#f59e0b', icon: 'fa-briefcase' },
      residential: { color: '#10b981', icon: 'fa-building' }
    };

    const style = type ? typeStyles[type] : undefined;

    if (style) {
      return this.createDivIcon({ color: style.color, icon: style.icon });
    }

    // Fallback to availability-based colors when type is unknown
    const safeTotal = total > 0 ? total : 1;
    if (available === 0) return this.createDivIcon({ color: '#757575', icon: 'fa-bicycle' });
    const pct = (available / safeTotal) * 100;
    if (pct > 50) return this.createDivIcon({ color: '#4caf50', icon: 'fa-bicycle' });
    if (pct > 20) return this.createDivIcon({ color: '#ff9800', icon: 'fa-bicycle' });
    return this.createDivIcon({ color: '#f44336', icon: 'fa-bicycle', pulse: true });
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
    const total = s.totalSlots && s.totalSlots > 0 ? s.totalSlots : 1;
    const pctNumber = (s.availableSlots/total)*100;
    const pct = pctNumber.toFixed(0);
    const color = s.availableSlots===0? '#757575': pctNumber>50? '#4caf50': pctNumber>20? '#ff9800': '#f44336';

    // Use i18n if available, otherwise fallback to hardcoded strings
    const t = this.t || ((key: string) => key);

    // Show mechanical and electric bikes for any station type (metro or bike)
    const mechanical = (s as any).mechanical ?? 0;
    const electric = (s as any).electric ?? 0;
    const bikeTypesHtml = `
      <div style='margin-top:5px;padding:5px;background:#f5f5f5;border-radius:3px'>
        <div style='font-weight:600;margin-bottom:3px;color:#374151;font-size:11px'>${t('reservation.map.popup.availableTypes')}</div>
        <div style='display:flex;gap:8px;font-size:11px'>
          <div><strong>⚙️ ${t('reservation.map.popup.mechanical')}:</strong> <span style='color:#2563eb;font-weight:600'>${mechanical}</span></div>
          <div><strong>⚡ ${t('reservation.map.popup.electric')}:</strong> <span style='color:#16a34a;font-weight:600'>${electric}</span></div>
        </div>
      </div>
    `;

    return `<div style='font-family:Arial;min-width:200px;max-width:280px'>
      <h4 style='margin:0 0 4px 0;color:#2c3e50;border-bottom:2px solid ${color};padding-bottom:3px;font-size:13px'>${(s as any).type === 'metro' ? '🚇' : '🅿️'} ${s.nameStation}</h4>
      <div style='font-size:11px;color:#444;margin-bottom:5px'>
        <strong>${t('reservation.map.popup.available')}:</strong> <span style='color:${color};font-weight:bold'>${s.availableSlots}</span>/<span>${s.totalSlots}</span>
        <span style='background:${color};color:#fff;padding:1px 4px;border-radius:3px;font-size:10px;margin-left:4px'>${pct}%</span>
        <div style='margin-top:2px;font-size:9px;color:#666'>Lat: ${s.latitude.toFixed(5)} | Lon: ${s.longitude.toFixed(5)}</div>
      </div>
      ${bikeTypesHtml}
      ${this.slotsHtml()}
    </div>`;
  }

  private slotsHtml(): string {
    const slots = this.station.slots;
    const t = this.t || ((key: string) => key);

    if (!slots || slots.length===0) return `<em style='color:#888;font-size:10px'>${t('reservation.map.popup.noSlots')}</em>`;
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
      return `<div title='${title}' style='width:22px;height:22px;background:${bg};color:#fff;border-radius:3px;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:600'>${icon}</div>`;
    }).join('');
    return `<div style='margin-top:5px'>
      <strong style='font-size:11px;color:#555;display:block;margin-bottom:3px'>${t('reservation.map.popup.slots')}</strong>
      <div style='display:grid;grid-template-columns:repeat(auto-fill,22px);gap:3px;max-height:100px;overflow:auto;padding:4px;background:#f5f5f5;border-radius:3px'>${cells}</div>
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
