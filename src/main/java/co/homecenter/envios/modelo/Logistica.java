package co.homecenter.envios.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA de la tabla {@code Logistica}: el personal de bodega que
 * prepara y despacha los envíos.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Logistica")
public class Logistica extends Usuario {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logistica")
    private Integer idLogistica;

    public Integer getIdLogistica() {
        return idLogistica;
    }

    public void setIdLogistica(Integer idLogistica) {
        this.idLogistica = idLogistica;
    }

    /**
     * Crea un responsable de logística que solo contiene la llave primaria.
     *
     * @param idLogistica llave primaria, o {@code null}
     * @return referencia con solo el id, o {@code null}
     */
    public static Logistica referencia(Integer idLogistica) {
        if (idLogistica == null) {
            return null;
        }
        Logistica logistica = new Logistica();
        logistica.setIdLogistica(idLogistica);
        return logistica;
    }
}
