/**
 * Panel de inicio (versión React de inicio.jsp).
 * El personal interno ve indicadores y pedidos recientes; el cliente ve sus pedidos.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */
import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { api } from '../api/clienteApi.js';
import { useSesion } from '../contexto/SesionContexto.jsx';
import Alerta from '../componentes/Alerta.jsx';
import Cargando from '../componentes/Cargando.jsx';
import EtiquetaEstado from '../componentes/EtiquetaEstado.jsx';
import { formatearMoneda } from '../utilidades/formato.js';

/** Tarjetas de indicadores: clave del JSON, texto y color. */
const TARJETAS = [
  { clave: 'pedidosActivos', texto: 'Pedidos en proceso', color: 'azul' },
  { clave: 'pedidosEntregados', texto: 'Pedidos entregados', color: 'verde' },
  { clave: 'enviosEnRuta', texto: 'Envíos en ruta', color: 'morado' },
  { clave: 'incidenciasAbiertas', texto: 'Incidencias abiertas', color: 'rojo' },
  { clave: 'clientes', texto: 'Clientes registrados', color: 'amarillo' },
];

export default function PaginaInicio() {
  const { usuario } = useSesion();
  const ubicacion = useLocation();
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState('');

  // Consulta /api/inicio al mostrar la pantalla.
  useEffect(() => {
    // "activo" evita actualizar el estado si el componente ya se desmontó (p. ej. al cambiar rápido de pantalla).
    let activo = true;
    api.consultarInicio()
      .then((respuesta) => activo && setDatos(respuesta))
      .catch((excepcion) => activo && setError(excepcion.message));
    return () => {
      activo = false;
    };
  }, []);

  const esInterno = usuario.personalInterno;

  return (
    <>
      <section className="banda-seccion">
        <div>
          <h2>Hola, {usuario.nombre}</h2>
          <p>{esInterno ? 'Resumen de la operación de pedidos y envíos.' : 'Aquí puede consultar el estado de sus pedidos.'}</p>
        </div>
        {esInterno && <Link className="boton boton--banda" to="/pedidos/nuevo">+ Nuevo pedido</Link>}
      </section>

      <Alerta tipo={ubicacion.state?.tipo ?? 'info'}>{ubicacion.state?.mensaje}</Alerta>
      <Alerta tipo="error">{error}</Alerta>

      {!datos && !error && <Cargando />}

      {datos?.indicadores && (
        <section className="indicadores" aria-label="Indicadores">
          {TARJETAS.map((tarjeta) => (
            <div key={tarjeta.clave} className={`indicador ${tarjeta.color}`}>
              <strong>{datos.indicadores[tarjeta.clave]}</strong>
              <span>{tarjeta.texto}</span>
            </div>
          ))}
        </section>
      )}

      {datos && (
        <section className="tarjeta">
          <h3 className="tarjeta__titulo">{esInterno ? 'Pedidos recientes' : 'Mis pedidos'}</h3>
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  <th>N.º</th><th>Fecha</th><th>Cliente</th><th>Vendedor</th><th>Estado</th>
                  <th className="numero">Total</th>
                </tr>
              </thead>
              <tbody>
                {datos.pedidos.map((pedido) => (
                  <tr key={pedido.idPedido}>
                    <td><strong>#{pedido.idPedido}</strong></td>
                    <td className="fecha">{pedido.fecha}</td>
                    <td>{pedido.nombreCliente}</td>
                    <td>{pedido.nombreVendedor}</td>
                    <td><EtiquetaEstado estado={pedido.estado} /></td>
                    <td className="numero">{formatearMoneda(pedido.total)}</td>
                  </tr>
                ))}
                {datos.pedidos.length === 0 && (
                  <tr><td colSpan={6} className="tabla-vacia">No hay pedidos para mostrar.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          {esInterno && <p><Link to="/pedidos">Ver todos los pedidos →</Link></p>}
        </section>
      )}
    </>
  );
}
