package com.movilidadsostenible.estaciones_service.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "station")
@Data
public class Station {

  @Id
  @Column(name = "k_id_station" )
  private int idStation;

  @Column(name = "n_station_name" )
  @NotBlank
  private String stationName;

  @Column(name = "n_latitude")
  @NotNull
  private Double latitude;

  @Column(name = "n_length")
  @NotNull
  private Double length;

  @Column(name = "k_id_city")
  @NotNull
  private Integer idCity;

  @Column(name = "t_type")
  @NotNull
  private String type;

  @Column(name = "t_cctv_status")
  private Boolean cctvStatus;
}
