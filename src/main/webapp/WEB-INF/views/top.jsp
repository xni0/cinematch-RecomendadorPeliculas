<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>CineMatch — Top Películas</title>
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
  <h1>Top películas</h1>
  <div class="toolbar">
    <a class="btn" href="<c:url value='/cine?op=HOME'/>">← Volver</a>
  </div>

  <c:choose>
    <c:when test="${not empty topPeliculas}">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Título</th>
            <th>Género</th>
            <th>Media global</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="p" items="${topPeliculas}" varStatus="st">
            <tr>
              <td>${st.index + 1}</td>
              <td>${p.titulo}</td>
              <td>${p.genero}</td>
              <td><fmt:formatNumber value="${p.puntuacionMedia}" minFractionDigits="1" maxFractionDigits="1"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:when>
    <c:otherwise>
      <p class="muted">No hay datos disponibles.</p>
    </c:otherwise>
  </c:choose>
</div>
</body>
</html>