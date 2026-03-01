package com.jorge.taxi.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un viaje dentro del sistema de gestión de taxis.
 *
 * <p>
 * Esta entidad almacena tanto los datos básicos del viaje (distancia,
 * duración y precio estimado), como información adicional necesaria
 * para análisis estadísticos y explotación de datos como:
 * zonas calientes, métricas por tipo de vehículo y estado del viaje.
 * </p>
 *
 * <p>
 * El precio estimado es calculado mediante un servicio externo de
 * Machine Learning antes de persistir la entidad.
 * </p>
 *
 * <ul>
 *   <li><b>distance_km:</b> distancia recorrida en kilómetros</li>
 *   <li><b>duration_min:</b> duración del viaje en minutos</li>
 *   <li><b>estimated_price:</b> precio calculado por el modelo ML</li>
 *   <li><b>origin_zone:</b> zona de origen del viaje</li>
 *   <li><b>destination_zone:</b> zona de destino del viaje</li>
 *   <li><b>vehicle_type:</b> tipo de vehículo utilizado</li>
 *   <li><b>status:</b> estado actual del viaje</li>
 *   <li><b>start_time:</b> fecha y hora de inicio del viaje</li>
 *   <li><b>end_time:</b> fecha y hora de finalización del viaje</li>
 *   <li><b>created_at:</b> fecha de creación del registro</li>
 * </ul>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.5
 */
@Entity
@Table(name = "trips")
public class Trip {

	/**
	 * Identificador único del viaje.
	 * <p>
	 * Se genera automáticamente mediante estrategia IDENTITY en la base de datos.
	 * Es la clave primaria de la entidad.
	 * </p>
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Distancia total del viaje expresada en kilómetros.
	 * <p>
	 * Se utiliza como uno de los parámetros principales para el cálculo
	 * del precio estimado mediante el modelo de Machine Learning.
	 * </p>
	 */
	@Column(nullable = false)
	private double distanceKm;

	/**
	 * Duración estimada o real del viaje en minutos.
	 * <p>
	 * Forma parte del conjunto de variables utilizadas para
	 * predecir el precio del trayecto.
	 * </p>
	 */
	@Column(nullable = false)
	private double durationMin;

	/**
	 * Precio estimado calculado por el modelo de Machine Learning.
	 * <p>
	 * Representa el coste proyectado del viaje en la moneda del sistema.
	 * Siempre debe ser un valor positivo.
	 * </p>
	 */
	@Column(nullable = false)
	private BigDecimal estimatedPrice;

	/*TODO: Esto de las rutas debería convertirlo en un futuro en 
	 * objetos, o usar la API de Google o algo para que sea lo
	 * más real posible, de momento se queda como un
	 * proyecto académico.
	 */
	/**
	 * Zona geográfica de origen del viaje.
	 * <p>
	 * Puede representar un barrio, distrito o área definida.
	 * Se utiliza para análisis estadísticos como zonas calientes.
	 * </p>
	 */
	@Column(nullable = false)
	private String originZone;

	/**
	 * Zona geográfica de destino del viaje.
	 * <p>
	 * Permite realizar análisis de frecuencia de trayectos entre zonas
	 * y cálculos de demanda por región.
	 * </p>
	 */
	@Column(nullable = false)
	private String destinationZone;

	/**
	 * Tipo de vehículo utilizado para realizar el viaje.
	 * <p>
	 * Se almacena como texto en base de datos mediante EnumType.STRING.
	 * Permite segmentar métricas por categoría de servicio.
	 * </p>
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VehicleType vehicleType;

	/**
	 * Estado actual del viaje dentro del ciclo de vida del sistema.
	 * <p>
	 * Controla la transición lógica del viaje
	 * (PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED).
	 * </p>
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TripStatus status;

	/**
	 * Fecha y hora en la que el viaje comienza.
	 * <p>
	 * Puede representar el momento en que el conductor inicia
	 * el trayecto con el pasajero.
	 * </p>
	 */
	@Column(nullable = false)
	private LocalDateTime startTime;

	/**
	 * Fecha y hora en la que el viaje finaliza.
	 * Puede ser nulo hasta que el viaje se complete.
	 */
	@Column(nullable = true)
	private LocalDateTime endTime;

	/**
	 * Fecha y hora en la que el registro del viaje fue creado.
	 * <p>
	 * Se establece automáticamente en el momento de persistencia
	 * y no puede ser actualizado posteriormente.
	 * </p>
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Constructor sin parámetros para crear una
	 * instancia de un viaje.
	 */
    public Trip() {}

    /**
     * Constructor principal para crear un viaje.
     *
     * @param distanceKm distancia en kilómetros
     * @param durationMin duración en minutos
     * @param estimatedPrice precio calculado por ML
     * @param originZone zona de origen
     * @param destinationZone zona de destino
     * @param vehicleType tipo de vehículo
     * @param status estado inicial del viaje
     * @param startTime fecha y hora de inicio
     */
    public Trip(double distanceKm,
                double durationMin,
                BigDecimal estimatedPrice,
                String originZone,
                String destinationZone,
                VehicleType vehicleType,
                TripStatus status,
                LocalDateTime startTime) {

        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.estimatedPrice = estimatedPrice;
        this.originZone = originZone;
        this.destinationZone = destinationZone;
        this.vehicleType = vehicleType;
        this.status = status;
        this.startTime = startTime;
        this.createdAt = LocalDateTime.now();
    }
    

    /**
     * Asigna automáticamente la fecha y hora de creación del viaje antes de
     * persistirlo en la base de datos.
     *
     * <p>Este método se ejecuta de forma automática gracias a la anotación
     * {@code @PrePersist}. Si el campo {@code created_at} aún no ha sido
     * establecido, se inicializa con el momento actual.</p>
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
	
	/**
	 * Acepta el viaje cambiando su estado de {@code PENDING} a {@code ACCEPTED}.
	 *
	 * <p>Este método debe ser invocado por el conductor cuando decide aceptar un viaje
	 * que aún no ha sido asignado definitivamente.</p>
	 *
	 * @throws IllegalStateException si el estado actual del viaje no es {@code PENDING}.
	 */
	public void accept() {
	    if (this.status != TripStatus.PENDING) {
	        throw new IllegalStateException("Trip must be PENDING to be accepted");
	    }
	    this.status = TripStatus.ACCEPTED;
	}

	/**
	 * Inicia el viaje cambiando su estado de {@code ACCEPTED} a {@code IN_PROGRESS}.
	 *
	 * <p>Este método representa el momento en que el pasajero sube al vehículo y el
	 * conductor comienza oficialmente el trayecto.</p>
	 *
	 * @throws IllegalStateException si el estado actual del viaje no es {@code ACCEPTED}.
	 */
	public void start() {
	    if (this.status != TripStatus.ACCEPTED) {
	        throw new IllegalStateException("Trip must be ACCEPTED to start");
	    }
	    this.status = TripStatus.IN_PROGRESS;
	}

	/**
	 * Completa el viaje cambiando su estado de {@code IN_PROGRESS} a {@code COMPLETED}.
	 *
	 * <p>Este método debe llamarse cuando el pasajero llega a su destino. Además,
	 * registra la hora de finalización del viaje.</p>
	 *
	 * @throws IllegalStateException si el estado actual del viaje no es {@code IN_PROGRESS}.
	 */
	public void complete() {
	    if (this.status != TripStatus.IN_PROGRESS) {
	        throw new IllegalStateException("Trip must be IN_PROGRESS to complete");
	    }
	    this.status = TripStatus.COMPLETED;
	    this.endTime = LocalDateTime.now();
	}

	/**
	 * Cancela el viaje cambiando su estado al valor {@code CANCELLED}.
	 *
	 * <p>Este método puede ser invocado tanto por el pasajero como por el conductor,
	 * siempre que el viaje no haya sido completado. Un viaje finalizado no puede
	 * cancelarse.</p>
	 *
	 * @throws IllegalStateException si el viaje ya está {@code COMPLETED}.
	 */
	public void cancel() {
	    if (this.status == TripStatus.COMPLETED) {
	        throw new IllegalStateException("Completed trip cannot be cancelled");
	    }
	    this.status = TripStatus.CANCELLED;
	}

	/**
	 * Devuelve el identificador único del viaje.
	 *
	 * @return id del viaje.
	 */
	public Long getId() {
	    return id;
	}

	/**
	 * Establece el identificador único del viaje.
	 *
	 * @param id valor que identificará el viaje en el sistema.
	 */
	public void setId(Long id) {
	    this.id = id;
	}

	/**
	 * Devuelve la distancia estimada del viaje en kilómetros.
	 *
	 * @return distancia en kilómetros.
	 */
	public double getDistanceKm() {
	    return distanceKm;
	}

	/**
	 * Establece la distancia estimada del viaje en kilómetros.
	 *
	 * @param distanceKm distancia total prevista del trayecto.
	 */
	public void setDistanceKm(double distanceKm) {
	    this.distanceKm = distanceKm;
	}

	/**
	 * Devuelve la duración estimada del viaje en minutos.
	 *
	 * @return duración en minutos.
	 */
	public double getDurationMin() {
	    return durationMin;
	}

	/**
	 * Establece la duración estimada del viaje en minutos.
	 *
	 * @param durationMin tiempo previsto para completar el trayecto.
	 */
	public void setDurationMin(double durationMin) {
	    this.durationMin = durationMin;
	}

	/**
	 * Devuelve el precio estimado del viaje.
	 *
	 * @return precio estimado como {@link BigDecimal}.
	 */
	public BigDecimal getEstimatedPrice() {
	    return estimatedPrice;
	}

	/**
	 * Establece el precio estimado del viaje.
	 *
	 * @param estimatedPrice coste aproximado calculado antes de iniciar el viaje.
	 */
	public void setEstimatedPrice(BigDecimal estimatedPrice) {
	    this.estimatedPrice = estimatedPrice;
	}

	/**
	 * Devuelve la zona de origen del viaje.
	 *
	 * @return zona de origen.
	 */
	public String getOriginZone() {
	    return originZone;
	}

	/**
	 * Establece la zona de origen del viaje.
	 *
	 * @param originZone ubicación inicial donde se recoge al pasajero.
	 */
	public void setOriginZone(String originZone) {
	    this.originZone = originZone;
	}

	/**
	 * Devuelve la zona de destino del viaje.
	 *
	 * @return zona de destino.
	 */
	public String getDestinationZone() {
	    return destinationZone;
	}

	/**
	 * Establece la zona de destino del viaje.
	 *
	 * @param destinationZone ubicación final donde se deja al pasajero.
	 */
	public void setDestinationZone(String destinationZone) {
	    this.destinationZone = destinationZone;
	}

	/**
	 * Devuelve el tipo de vehículo solicitado para el viaje.
	 *
	 * @return tipo de vehículo.
	 */
	public VehicleType getVehicleType() {
	    return vehicleType;
	}

	/**
	 * Establece el tipo de vehículo solicitado para el viaje.
	 *
	 * @param vehicleType categoría del vehículo (por ejemplo, estándar, XL, lujo).
	 */
	public void setVehicleType(VehicleType vehicleType) {
	    this.vehicleType = vehicleType;
	}

	/**
	 * Devuelve el estado actual del viaje.
	 *
	 * @return estado del viaje.
	 */
	public TripStatus getStatus() {
	    return status;
	}

	/**
	 * Establece el estado actual del viaje.
	 *
	 * <p>Este método debe usarse con precaución, ya que el flujo de estados
	 * suele estar controlado por métodos específicos como {@code accept()},
	 * {@code start()}, {@code complete()} o {@code cancel()}.</p>
	 *
	 * @param status nuevo estado del viaje.
	 */
	public void setStatus(TripStatus status) {
	    this.status = status;
	}

	/**
	 * Devuelve la fecha y hora en que comenzó el viaje.
	 *
	 * @return fecha y hora de inicio.
	 */
	public LocalDateTime getStartTime() {
	    return startTime;
	}

	/**
	 * Establece la fecha y hora de inicio del viaje.
	 *
	 * @param startTime momento en que el conductor inicia el trayecto.
	 */
	public void setStartTime(LocalDateTime startTime) {
	    this.startTime = startTime;
	}

	/**
	 * Devuelve la fecha y hora en que finalizó el viaje.
	 *
	 * @return fecha y hora de finalización, o {@code null} si aún no ha terminado.
	 */
	public LocalDateTime getEndTime() {
	    return endTime;
	}

	/**
	 * Establece la fecha y hora de finalización del viaje.
	 *
	 * @param endTime momento en que el viaje se da por completado.
	 */
	public void setEndTime(LocalDateTime endTime) {
	    this.endTime = endTime;
	}

	/**
	 * Devuelve la fecha y hora en que se creó el registro del viaje.
	 *
	 * @return fecha de creación.
	 */
	public LocalDateTime getCreatedAt() {
	    return createdAt;
	}

	/**
	 * Establece la fecha y hora de creación del viaje.
	 *
	 * @param createdAt momento en que el viaje fue registrado en el sistema.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
	    this.createdAt = createdAt;
	}

	/**
	 * Calcula el código hash del viaje utilizando un conjunto de atributos
	 * relevantes para identificar de forma consistente la instancia.
	 *
	 * <p>Este método es coherente con {@link #equals(Object)} y permite que
	 * los objetos {@code Trip} funcionen correctamente en estructuras de datos
	 * basadas en hashing, como {@code HashSet} o {@code HashMap}.</p>
	 *
	 * @return valor hash calculado a partir de los atributos del viaje.
	 */
	@Override
	public int hashCode() {
	    return Objects.hash(createdAt, destinationZone, distanceKm, durationMin, endTime,
	            estimatedPrice, id, originZone, startTime, status, vehicleType);
	}

	/**
	 * Compara este viaje con otro objeto para determinar si ambos representan
	 * la misma información.
	 *
	 * <p>Dos instancias de {@code Trip} se consideran iguales si todos los
	 * atributos significativos coinciden: identificador, zonas, distancia,
	 * duración, precio estimado, fechas, estado y tipo de vehículo.</p>
	 *
	 * <p>Este método es coherente con {@link #hashCode()} y permite un
	 * comportamiento correcto en colecciones que dependen de igualdad lógica.</p>
	 *
	 * @param obj objeto con el que se compara esta instancia.
	 * @return {@code true} si ambos objetos representan el mismo viaje,
	 *         {@code false} en caso contrario.
	 */
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	    Trip other = (Trip) obj;
	    return Objects.equals(createdAt, other.createdAt)
	            && Objects.equals(destinationZone, other.destinationZone)
	            && Double.doubleToLongBits(distanceKm) == Double.doubleToLongBits(other.distanceKm)
	            && Double.doubleToLongBits(durationMin) == Double.doubleToLongBits(other.durationMin)
	            && Objects.equals(endTime, other.endTime)
	            && Objects.equals(estimatedPrice, other.estimatedPrice)
	            && Objects.equals(id, other.id)
	            && Objects.equals(originZone, other.originZone)
	            && Objects.equals(startTime, other.startTime)
	            && status == other.status
	            && vehicleType == other.vehicleType;
	}

	/**
	 * Devuelve una representación textual del viaje, incluyendo sus atributos
	 * principales como identificador, distancia, duración, precio estimado,
	 * zonas de origen y destino, tipo de vehículo, estado y marcas de tiempo.
	 *
	 * <p>Este método es útil para tareas de depuración, registro de actividad
	 * (logging) y para inspeccionar rápidamente el contenido de una instancia
	 * de {@code Trip} durante el desarrollo o en trazas del sistema.</p>
	 *
	 * @return una cadena con los valores relevantes del viaje.
	 */
	@Override
	public String toString() {
	    return "Trip [id=" + id + ", distance_km=" + distanceKm + ", duration_min=" + durationMin
	            + ", estimated_price=" + estimatedPrice + ", origin_zone=" + originZone + ", destination_zone="
	            + destinationZone + ", vehicle_type=" + vehicleType + ", status=" + status + ", start_time="
	            + startTime + ", end_time=" + endTime + ", created_at=" + createdAt + "]";
	}
    
    
}