package controller; 


import model.CineService;
import entidades.Pelicula;
import entidades.UsuarioPelicula;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List; 
/**
 * Servlet principal (paquete 'controller').
 * Gestiona todas las peticiones (GET y POST) con PRG.
 */
@WebServlet(name = "CineController", value = "/cine")
public class CineController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private CineService cineService;
    
    // Constantes
    private static final int HISTORIAL_PAGE_SIZE = 5; 
    private static final int TOP_PELICULAS_LIMIT = 10;
    private static final int ULTIMAS_ALIAS_LIMIT = 3; 

    // init() se ejecuta solo 1 vez
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Creamos el Service
        cineService = new CineService();
        // Guardamos los géneros en el contexto (ámbito aplicación)
        config.getServletContext().setAttribute("generos", cineService.getGeneros());
    }

  
    //   doGET: SOLO para MOSTRAR VISTAS

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String op = request.getParameter("op");
        if (op == null) {
            op = "HOME"; // Operación por defecto
        }

        String vistaJSP; 

        switch (op) {
            case "HOME":
                // 1. Mover datos del PRG (de Sesión a Request)
                transferirDatosPRG(request);
                
                // 2. Cargamos las "ultimas" del alias (si hay alias)
                String aliasActual = (String) request.getAttribute("aliasActual");
                if (aliasActual != null && !aliasActual.isBlank()) {
                    List<UsuarioPelicula> ultimas = cineService.getUltimas(aliasActual, ULTIMAS_ALIAS_LIMIT);
                    request.setAttribute("ultimas", ultimas);
                }
                
                vistaJSP = "/WEB-INF/views/home.jsp"; // <-- ¡CORREGIDO!
                break;

            case "CONSULTAR_HISTORIAL":
                // 1. Mover el mensaje flash (si venimos de borrar)
                transferirDatosPRG(request);
                
                String aliasHistorial = request.getParameter("alias");
                int paginaActual = 0;
                if (request.getParameter("page") != null) {
                    try {
                        paginaActual = Integer.parseInt(request.getParameter("page"));
                    } catch (NumberFormatException e) {
                        paginaActual = 0; 
                    }
                }

                if (aliasHistorial != null && !aliasHistorial.isBlank()) {
                    List<UsuarioPelicula> historial = cineService.handleConsultarHistorial(aliasHistorial, paginaActual, HISTORIAL_PAGE_SIZE);
                    int totalRegistros = cineService.getTotalHistorial(aliasHistorial);
                    
                    boolean hayMas = (long) (paginaActual + 1) * HISTORIAL_PAGE_SIZE < totalRegistros;
                    
                    request.setAttribute("historial", historial);
                    request.setAttribute("alias", aliasHistorial);
                    request.setAttribute("paginaActual", paginaActual);
                    request.setAttribute("paginado", true);
                    request.setAttribute("hayMas", hayMas);
                }
                
                vistaJSP = "/WEB-INF/views/historial.jsp";
                break;

            case "TOP_PELICULAS":
                // Aquí faltaba pedir los datos al service
                List<Pelicula> top = cineService.handleTopPeliculas(TOP_PELICULAS_LIMIT);
                request.setAttribute("topPeliculas", top);
                vistaJSP = "/WEB-INF/views/top.jsp";
                break;

            default:
                vistaJSP = "/WEB-INF/views/home.jsp";
        }

        // 3. (Forward) Despachar al JSP
        request.getRequestDispatcher(vistaJSP).forward(request, response);
    }


    //   doPOST: SOLO para PROCESAR ACCIONES (formularios)

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); 

        // 1. Recoger parámetros del formulario
        String op = request.getParameter("operacion");
        String alias = request.getParameter("alias");
        String genero = request.getParameter("genero");
        String valoracionStr = request.getParameter("valoracion");

        HttpSession session = request.getSession();
        String urlRedireccion = request.getContextPath() + "/cine?op=HOME"; 

        // --- INICIO PRG (Paso 1: Post) ---
        // Guardamos los datos del formulario en SESIÓN
        session.setAttribute("aliasActual", alias);
        session.setAttribute("generoActual", genero);
        session.setAttribute("valoracionActual", valoracionStr);
        
        // 2. Switch para procesar la operación
        switch (op) {
            case "REGISTRAR":
                try {
                    Double valoracion = (valoracionStr != null && !valoracionStr.isBlank()) 
                                        ? Double.parseDouble(valoracionStr) : null;
                                        
                    String msg = cineService.handleRegistrar(alias, genero, valoracion);
                    session.setAttribute("flash", msg); 
                    
                } catch (NumberFormatException e) {
                    session.setAttribute("flash", "Error: La valoración debe ser un número.");
                }
                break; 

            case "RECOMENDAR":
                Pelicula p = cineService.handleRecomendar(genero);
                if (p != null) {
                    session.setAttribute("recomendacion", p);
                } else {
                    session.setAttribute("flash", "No hay recomendaciones para " + genero);
                }
                break;

            case "CONSULTAR_HISTORIAL":
                urlRedireccion = request.getContextPath() + "/cine?op=CONSULTAR_HISTORIAL&alias=" + alias;
                break;

            case "BORRAR_HISTORIAL":
                String msgBorrar = cineService.handleBorrarHistorial(alias);
                session.setAttribute("flash", msgBorrar);
                urlRedireccion = request.getContextPath() + "/cine?op=CONSULTAR_HISTORIAL&alias=" + alias;
                break;

            case "TOP_PELICULAS":
                urlRedireccion = request.getContextPath() + "/cine?op=TOP_PELICULAS";
                break;
        }

        // 3. --- INICIO PRG (Paso 2: Redirect) ---
        response.sendRedirect(urlRedireccion);
    }

    /**
     * --- INICIO PRG (Paso 3: Get) ---
     * Mueve los datos de la Sesión (donde los puso doPost)
     * a la Request (para que los lea el JSP).
     */
    private void transferirDatosPRG(HttpServletRequest request) {
        HttpSession session = request.getSession();

        if (session.getAttribute("flash") != null) {
            request.setAttribute("flash", session.getAttribute("flash"));
            session.removeAttribute("flash"); 
        }

        if (session.getAttribute("recomendacion") != null) {
            request.setAttribute("recomendacion", session.getAttribute("recomendacion"));
            session.removeAttribute("recomendacion");
        }
        
        if (session.getAttribute("aliasActual") != null) {
            request.setAttribute("aliasActual", session.getAttribute("aliasActual"));
            session.removeAttribute("aliasActual");
        }
        if (session.getAttribute("generoActual") != null) {
            request.setAttribute("generoActual", session.getAttribute("generoActual"));
            session.removeAttribute("generoActual");
        }
        if (session.getAttribute("valoracionActual") != null) {
            request.setAttribute("valoracionActual", session.getAttribute("valoracionActual"));
            session.removeAttribute("valoracionActual");
        }
    }
}