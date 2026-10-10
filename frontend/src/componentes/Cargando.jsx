/**
 * Indicador de carga mientras se esperan datos del servidor.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * @param {{ texto?: string }} props
 */
export default function Cargando({ texto = 'Cargando…' }) {
  return (
    <div className="cargando" role="status" aria-live="polite">
      <span className="cargando__circulo" aria-hidden="true" />
      {texto}
    </div>
  );
}
