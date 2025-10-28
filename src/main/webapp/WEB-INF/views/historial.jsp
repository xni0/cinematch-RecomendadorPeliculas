<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>CineMatch — Historial</title>
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <style>
    body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;margin:1rem}
    .wrap{max-width:980px;margin:auto}
    table{width:100%;border-collapse:collapse}
    th,td{padding:.6rem;border-bottom:1px solid #eee;text-align:left}
    .toolbar{display:flex;gap:.6rem;align-items:center;margin:.6rem 0}
    .btn{padding:.5rem .9rem;border:1px solid #222;border-radius:10px;background:#fff;text-decoration:none}
    .muted{color:#666}
  </style>
</head>
<body>
<div class="wrap">
  <h1>Historial de <c:out value="${alias}" /></h1>
  <div class="toolbar">
    <a class="btn" href="<c:url value='/cine?op=HOME'/>">← Volver</a>
    <c:if test="${not empty alias}">
      <a class="btn" href="<c:url value='/cine?op=BORRAR_HISTORIAL&alias=${fn:escapeXml(alias)}'/>">Borrar historial</a>
    </c:if>
  </div>

  <c:choose>
    <c:when test="${not empty historial}">
      <table>
        <thead>
          <tr>
            <th>Título</th>
            <th>Género</th>
            <th>Mi valoración</th>
            <th>Fecha</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="item" items="${historial}">
            <tr>
              <td>${item.pelicula.titulo}</td>
              <td>${item.pelicula.genero}</td>
              <td><fmt:formatNumber value="${item.valoracion}" minFractionDigits="1" maxFractionDigits="1"/></td>
              <td><fmt:formatDate value="${item.fechaRegistro}" pattern="yyyy-MM-dd HH:mm"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>

      <c:if test="${paginado}">
        <div class="toolbar">
          <c:if test="${paginaActual > 0}">
            <a class="btn" href="<c:url value='/cine?op=CONSULTAR_HISTORIAL&alias=${fn:escapeXml(alias)}&page=${paginaActual-1}'/>">« Anterior</a>
          </c:if>
          <span class="muted">Página ${paginaActual + 1}</span>
          <c:if test="${hayMas}">
            <a class="btn" href="<c:url value='/cine?op=CONSULTAR_HISTORIAL&alias=${fn:escapeXml(alias)}&page=${paginaActual+1}'/>">Siguiente »</a>
          </c:if>
        </div>
      </c:if>
    </c:when>
    <c:otherwise>
      <p class="muted">No hay registros para este alias.</p>
    </c:otherwise>
  </c:choose>
</div>
</body>
</html>

