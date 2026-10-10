/**
 * Pantalla para rutas inexistentes (equivale al error 404 de error.jsp).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */
import { Link } from 'react-router-dom';

export default function PaginaNoEncontrada() {
  return (
    <section className="tarjeta pagina-error">
      <p className="codigo">404</p>
      <h2>Página no encontrada</h2>
      <p className="texto-secundario">La dirección que buscó no existe o fue movida.</p>
      <Link className="boton boton--primario" to="/">Volver al inicio</Link>
    </section>
  );
}
