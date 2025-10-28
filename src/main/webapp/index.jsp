<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>CineMatch — Inicio</title>
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <style>
    body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;margin:2rem;}
    .card{max-width:720px;margin:auto;padding:1.25rem;border:1px solid #ddd;border-radius:12px;}
    .btn{display:inline-block;padding:.6rem 1rem;border:1px solid #222;border-radius:10px;text-decoration:none}
  </style>
</head>
<body>
  <div class="card">
    <h1>CineMatch</h1>
    <p>Recomendador de películas con MVC + DAO + PRG.</p>
    <p>
      <a class="btn" href="<c:url value='/cine?op=HOME'/>">Entrar</a>
    </p>
  </div>
</body>
</html>