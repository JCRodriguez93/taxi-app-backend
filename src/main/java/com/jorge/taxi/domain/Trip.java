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
 * @version 1.0.3
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
	private double distance_km;

	/**
	 * Duración estimada o real del viaje en minutos.
	 * <p>
	 * Forma parte del conjunto de variables utilizadas para
	 * predecir el precio del trayecto.
	 * </p>
	 */
	@Column(nullable = false)
	private double duration_min;

	/**
	 * Precio estimado calculado por el modelo de Machine Learning.
	 * <p>
	 * Representa el coste proyectado del viaje en la moneda del sistema.
	 * Siempre debe ser un valor positivo.
	 * </p>
	 */
	@Column(nullable = false)
	private BigDecimal estimated_price;

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
	private String origin_zone;

	/**
	 * Zona geográfica de destino del viaje.
	 * <p>
	 * Permite realizar análisis de frecuencia de trayectos entre zonas
	 * y cálculos de demanda por región.
	 * </p>
	 */
	@Column(nullable = false)
	private String destination_zone;

	/**
	 * Tipo de vehículo utilizado para realizar el viaje.
	 * <p>
	 * Se almacena como texto en base de datos mediante EnumType.STRING.
	 * Permite segmentar métricas por categoría de servicio.
	 * </p>
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VehicleType vehicle_type;

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
	private LocalDateTime start_time;

	/**
	 * Fecha y hora en la que el viaje finaliza.
	 * Puede ser nulo hasta que el viaje se complete.
	 */
	@Column(nullable = true)
	private LocalDateTime end_time;

	/**
	 * Fecha y hora en la que el registro del viaje fue creado.
	 * <p>
	 * Se establece automáticamente en el momento de persistencia
	 * y no puede ser actualizado posteriormente.
	 * </p>
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime created_at;

	/**
	 * Constructor sin parámetros para crear una
	 * instancia de un viaje.
	 */
    public Trip() {}

    /**
     * Constructor principal para crear un viaje.
     *
     * @param distance_km distancia en kilómetros
     * @param duration_min duración en minutos
     * @param estimated_price precio calculado por ML
     * @param origin_zone zona de origen
     * @param destination_zone zona de destino
     * @param vehicle_type tipo de vehículo
     * @param status estado inicial del viaje
     * @param start_time fecha y hora de inicio
     */
    public Trip(double distance_km,
                double duration_min,
                BigDecimal estimated_price,
                String origin_zone,
                String destination_zone,
                VehicleType vehicle_type,
                TripStatus status,
                LocalDateTime start_time) {

        this.distance_km = distance_km;
        this.duration_min = duration_min;
        this.estimated_price = estimated_price;
        this.origin_zone = origin_zone;
        this.destination_zone = destination_zone;
        this.vehicle_type = vehicle_type;
        this.status = status;
        this.start_time = start_time;
        this.created_at = LocalDateTime.now();
    }
    
    

    /**
     * Crea una nueva instancia de un viaje utilizando únicamente los datos
     * necesarios para el cálculo del precio estimado mediante modelos de
     * machine learning.
     *
     * <p>Este constructor se emplea cuando el sistema necesita generar una
     * predicción del coste del viaje antes de que exista un registro completo
     * en la base de datos. Por ello, solo incluye distancia, duración y
     * precio estimado.</p>
     *
     * @param distance_km distancia estimada del viaje en kilómetros.
     * @param duration_min duración estimada del viaje en minutos.
     * @param estimated_price precio estimado calculado por el modelo.
     */
    public Trip(double distance_km, double duration_min, BigDecimal estimated_price) {
        super();
        this.distance_km = distance_km;
        this.duration_min = duration_min;
        this.estimated_price = estimated_price;
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
        if (created_at == null) {
            created_at = LocalDateTime.now();
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
	    this.end_time = LocalDateTime.now();
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
	public double getDistance_km() {
	    return distance_km;
	}

	/**
	 * Establece la distancia estimada del viaje en kilómetros.
	 *
	 * @param distance_km distancia total prevista del trayecto.
	 */
	public void setDistance_km(double distance_km) {
	    this.distance_km = distance_km;
	}

	/**
	 * Devuelve la duración estimada del viaje en minutos.
	 *
	 * @return duración en minutos.
	 */
	public double getDuration_min() {
	    return duration_min;
	}

	/**
	 * Establece la duración estimada del viaje en minutos.
	 *
	 * @param duration_min tiempo previsto para completar el trayecto.
	 */
	public void setDuration_min(double duration_min) {
	    this.duration_min = duration_min;
	}

	/**
	 * Devuelve el precio estimado del viaje.
	 *
	 * @return precio estimado como {@link BigDecimal}.
	 */
	public BigDecimal getEstimated_price() {
	    return estimated_price;
	}

	/**
	 * Establece el precio estimado del viaje.
	 *
	 * @param estimated_price coste aproximado calculado antes de iniciar el viaje.
	 */
	public void setEstimated_price(BigDecimal estimated_price) {
	    this.estimated_price = estimated_price;
	}

	/**
	 * Devuelve la zona de origen del viaje.
	 *
	 * @return zona de origen.
	 */
	public String getOrigin_zone() {
	    return origin_zone;
	}

	/**
	 * Establece la zona de origen del viaje.
	 *
	 * @param origin_zone ubicación inicial donde se recoge al pasajero.
	 */
	public void setOrigin_zone(String origin_zone) {
	    this.origin_zone = origin_zone;
	}

	/**
	 * Devuelve la zona de destino del viaje.
	 *
	 * @return zona de destino.
	 */
	public String getDestination_zone() {
	    return destination_zone;
	}

	/**
	 * Establece la zona de destino del viaje.
	 *
	 * @param destination_zone ubicación final donde se deja al pasajero.
	 */
	public void setDestination_zone(String destination_zone) {
	    this.destination_zone = destination_zone;
	}

	/**
	 * Devuelve el tipo de vehículo solicitado para el viaje.
	 *
	 * @return tipo de vehículo.
	 */
	public VehicleType getVehicle_type() {
	    return vehicle_type;
	}

	/**
	 * Establece el tipo de vehículo solicitado para el viaje.
	 *
	 * @param vehicle_type categoría del vehículo (por ejemplo, estándar, XL, lujo).
	 */
	public void setVehicle_type(VehicleType vehicle_type) {
	    this.vehicle_type = vehicle_type;
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
	public LocalDateTime getStart_time() {
	    return start_time;
	}

	/**
	 * Establece la fecha y hora de inicio del viaje.
	 *
	 * @param start_time momento en que el conductor inicia el trayecto.
	 */
	public void setStart_time(LocalDateTime start_time) {
	    this.start_time = start_time;
	}

	/**
	 * Devuelve la fecha y hora en que finalizó el viaje.
	 *
	 * @return fecha y hora de finalización, o {@code null} si aún no ha terminado.
	 */
	public LocalDateTime getEnd_time() {
	    return end_time;
	}

	/**
	 * Establece la fecha y hora de finalización del viaje.
	 *
	 * @param end_time momento en que el viaje se da por completado.
	 */
	public void setEnd_time(LocalDateTime end_time) {
	    this.end_time = end_time;
	}

	/**
	 * Devuelve la fecha y hora en que se creó el registro del viaje.
	 *
	 * @return fecha de creación.
	 */
	public LocalDateTime getCreated_at() {
	    return created_at;
	}

	/**
	 * Establece la fecha y hora de creación del viaje.
	 *
	 * @param created_at momento en que el viaje fue registrado en el sistema.
	 */
	public void setCreated_at(LocalDateTime created_at) {
	    this.created_at = created_at;
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
	    return Objects.hash(created_at, destination_zone, distance_km, duration_min, end_time,
	            estimated_price, id, origin_zone, start_time, status, vehicle_type);
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
	    return Objects.equals(created_at, other.created_at)
	            && Objects.equals(destination_zone, other.destination_zone)
	            && Double.doubleToLongBits(distance_km) == Double.doubleToLongBits(other.distance_km)
	            && Double.doubleToLongBits(duration_min) == Double.doubleToLongBits(other.duration_min)
	            && Objects.equals(end_time, other.end_time)
	            && Objects.equals(estimated_price, other.estimated_price)
	            && Objects.equals(id, other.id)
	            && Objects.equals(origin_zone, other.origin_zone)
	            && Objects.equals(start_time, other.start_time)
	            && status == other.status
	            && vehicle_type == other.vehicle_type;
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
	    return "Trip [id=" + id + ", distance_km=" + distance_km + ", duration_min=" + duration_min
	            + ", estimated_price=" + estimated_price + ", origin_zone=" + origin_zone + ", destination_zone="
	            + destination_zone + ", vehicle_type=" + vehicle_type + ", status=" + status + ", start_time="
	            + start_time + ", end_time=" + end_time + ", created_at=" + created_at + "]";
	}
    
    
}