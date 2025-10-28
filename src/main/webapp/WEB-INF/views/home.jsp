
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>CineMatch — Home</title> 
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <style>
    :root{--gap:.8rem}
    body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;margin:1rem}
    header,main{max-width:980px;margin:auto}
    nav a{margin-right:.6rem}
    .grid{display:grid;grid-template-columns:1fr;gap:var(--gap)}
    .row{display:grid;grid-template-columns:1fr 2fr;gap:var(--gap);align-items:center}
    .card{border:1px solid #ddd;border-radius:12px;padding:1rem}
    .btn{padding:.6rem 1rem;border:1px solid #222;border-radius:10px;cursor:pointer;background:#fff}
    .flash{background:#f6fff1;border:1px solid #b8e0a0;padding:.6rem 1rem;border-radius:8px;margin:.6rem 0}
    table{width:100%;border-collapse:collapse}
    th,td{padding:.5rem;border-bottom:1px solid #eee;text-align:left}
    small.muted{color:#666}
    @media(min-width:760px){ .grid-2{grid-template-columns:2fr 1fr} }
  </style>
</head>
<body>
<header
class="grid">
  <div>
    <h1>CineMatch</h1>
    <small class="muted">PRG + MVC + DAO (JDBC).
Vistas sin lógica: JSTL + EL.</small>
  </div>
  <nav>
    <a href="<c:url value='/cine?op=HOME'/>">Inicio</a>
    <a href="<c:url value='/cine?op=TOP_PELICULAS'/>">Top películas</a>
    <c:if test="${not empty aliasActual}">
      <%-- Usamos fn:escapeXml por seguridad, si no lo tienes quita el "fn:" --%>
      <a href="<c:url value='/cine?op=CONSULTAR_HISTORIAL&alias=${fn:escapeXml(aliasActual)}'/>">Mi historial</a>
    </c:if>
  </nav>
</header>

<main class="grid grid-2">
  <section class="card">
    <h2>Formulario</h2>
    <c:if test="${not empty flash}">
      <div class="flash">${flash}</div>
    </c:if>

    <form method="post" action="<c:url value='/cine'/>" class="grid">
      <div class="row">
        <label
for="alias">Alias</label>
        <input type="text" id="alias" name="alias" value="${aliasActual}" required />
      </div>

      <div class="row">
        <label for="genero">Género favorito</label>
        <select id="genero" name="genero" required>
          <%-- El 'generos' lo coge del ServletContext (scope aplicación) --%>
          <c:forEach var="g" items="${generos}">
            <option value="${g}" <c:if test="${g == generoActual}">selected</c:if>>${g}</option>
          </c:forEach>
        </select>
    
  </div>

      <div class="row">
        <label for="valoracion">Valoración personal media (1–5)</label>
        <input type="number" id="valoracion" name="valoracion" min="1" max="5" step="0.1" value="${valoracionActual}" />
      </div>

      <div class="row">
        <label for="operacion">Operación</label>
        <select id="operacion" name="operacion" required>
          <option value="REGISTRAR">REGISTRAR</option>
          <option value="RECOMENDAR">RECOMENDAR</option>
        
  <option value="CONSULTAR_HISTORIAL">CONSULTAR_HISTORIAL</option>
          <option value="BORRAR_HISTORIAL">BORRAR_HISTORIAL</option>
          <option value="TOP_PELICULAS">TOP_PELICULAS</option>
        </select>
      </div>

      <div>
        <button class="btn" type="submit">Enviar</button>
      </div>
    </form>
  </section>

  <aside class="card">
    <h3>Resultado</h3>

    <%-- Aquí muestra la recomendación si existe --%>
    <c:if test="${not empty recomendacion}">
      <p><strong>Recomendación:</strong> ${recomendacion.titulo}</p>
   
  <p><small>Género:</small> ${recomendacion.genero}</p>
      <p><small>Media global:</small> <fmt:formatNumber value="${recomendacion.puntuacionMedia}" minFractionDigits="1" maxFractionDigits="1"/></p>
    </c:if>

    <c:if test="${empty recomendacion}">
      <p class="muted">No hay recomendación disponible todavía.
Elige “RECOMENDAR”.</p>
    </c:if>

    <hr/>
    <h4>Últimas del alias</h4>
    <c:choose>
      <%-- Aquí muestra las 'ultimas' que carga el case "HOME": del controller --%>
      <c:when test="${not empty ultimas}">
        <table>
          <thead><tr><th>Título</th><th>Género</th><th>Mi val.</th></tr></thead>
          <tbody>
            <c:forEach var="up" items="${ultimas}">
              <tr>
              
  <td>${up.pelicula.titulo}</td>
                <td>${up.pelicula.genero}</td>
                <td><fmt:formatNumber value="${up.valoracion}" minFractionDigits="1" maxFractionDigits="1"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
       
<p class="muted">Sin registros aún.</p>
      </c:otherwise>
    </c:choose>
  </aside>
</main>
</body>
</html>