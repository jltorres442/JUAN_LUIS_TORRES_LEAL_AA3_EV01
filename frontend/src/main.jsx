/**
 * Punto de entrada de la aplicación React.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * 1. Importa la hoja de estilos COMPARTIDA con la versión JSP
 *    (src/main/webapp/css/estilos.css): misma paleta corporativa (#0B4EA2,
 *    #F5B400, #D32F2F, #388E3C) y misma tipografía Segoe UI / Roboto.
 * 2. Importa los estilos propios de React (complementos.css).
 * 3. Monta el componente <App /> dentro de <div id="raiz"> de index.html.
 */
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '../../src/main/webapp/css/estilos.css';
import './estilos/complementos.css';
import App from './App.jsx';

createRoot(document.getElementById('raiz')).render(
  // StrictMode activa advertencias adicionales durante el desarrollo; no afecta la versión final.
  <StrictMode>
    <App />
  </StrictMode>,
);
