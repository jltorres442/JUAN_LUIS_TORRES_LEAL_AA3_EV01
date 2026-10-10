/**
 * Etiqueta de color para el estado de un pedido, envío o incidencia.
 * El color lo define la clase CSS etiqueta--{ESTADO} de estilos.css
 * (naranja = pendiente, morado = en ruta, verde = entregado, rojo = problema).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */
import { formatearEstado } from '../utilidades/formato.js';

/**
 * @param {{ estado: string }} props estado tal como viene de la base de datos
 */
export default function EtiquetaEstado({ estado }) {
  return <span className={`etiqueta etiqueta--${estado}`}>{formatearEstado(estado)}</span>;
}
