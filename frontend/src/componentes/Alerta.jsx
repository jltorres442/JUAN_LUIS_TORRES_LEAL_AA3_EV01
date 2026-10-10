/**
 * Mensaje de retroalimentación (éxito o error) en la parte superior de una pantalla.
 * Equivale al fragmento mensajes.jsp de la versión JSP y usa sus mismas clases CSS.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * @param {{ tipo?: 'exito'|'error'|'info', children: React.ReactNode }} props
 */
export default function Alerta({ tipo = 'info', children }) {
  // Si no hay mensaje no se dibuja nada (return null es válido en React).
  if (!children) {
    return null;
  }
  const iconos = { exito: '✔', error: '⚠', info: 'ℹ' };
  return (
    // role="alert" hace que los lectores de pantalla anuncien los errores de inmediato.
    <div className={`alerta alerta--${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      {iconos[tipo]} {children}
    </div>
  );
}
