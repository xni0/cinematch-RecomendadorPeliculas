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
public class CineController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private CineService cineService;
    
    // Constantes
    private static final int HISTORIAL_PAGE_SIZE = 5; 
    private static final int TOP_PELICULAS_LIMIT = 10;
    private static final int ULTIMAS_ALIAS_LIMIT = 3; 

    // init() se ejecuta solo 1 vez, cuando el servidor Payara carga la aplicación
    // método de arranque del servlet
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Creamos el Service
        // CineService va al DAO, coge los datos de la BBDD y los devuelve al doGet
        cineService = new CineService();
        // Guardamos los géneros en el contexto (ámbito aplicación)
        config.getServletContext().setAttribute("generos", cineService.getGeneros());
    }

  
    //   doGET: SOLO para MOSTRAR VISTAS

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    	
    	// recoge los parametros
        String op = request.getParameter("op");
        if (op == null) {
            op = "HOME"; // * Operación por defecto
        }

        String vistaJSP; 

        // * entra en el switch
        switch (op) {
            case "HOME":
                // 1. Mover datos del PRG (de Sesión a Request)
            	
            	// * si no se viene de un formulario NO HACE NADA.
            	// SOLO SE EJECUTA EN EL ULTIMO GET PARA HACER EL PRG
                transferirDatosPRG(request);
                
                // 2. Cargamos las "ultimas" del alias (si hay alias)
                // (busca en la sesión)
                // busca flash
                
                // ultimo doGet --> consigue el aliasActual al fin, el servicio va a la BBDD y trae la lista
                // de peliculas
                String aliasActual = (String) request.getAttribute("aliasActual");
                if (aliasActual != null && !aliasActual.isBlank()) {
                	// * Ahora que tiene el aliasActual en la request, llama a cineService.getUltimas
                	// * El servicio va al DAO que va a la BD y trae la lista de películas
                    List<UsuarioPelicula> ultimas = cineService.getUltimas(aliasActual, ULTIMAS_ALIAS_LIMIT);
                    //* Guarda esa lista en la Request  
                    request.setAttribute("ultimas", ultimas);
                }
                
                // * la variable vistaJSP toma el valor "/WEB-INF/views/home.jsp".
                vistaJSP = "/WEB-INF/views/home.jsp"; 
                break;

            case "CONSULTAR_HISTORIAL":
                // 1. Mover el mensaje flash (si venimos de borrar) --> "Historial borrado"
                transferirDatosPRG(request);
                
                String aliasHistorial = request.getParameter("alias");
                int paginaActual = 0;
                // comprueba si la URL tiene parámetro (ej, page=1)
                if (request.getParameter("page") != null) {
                    try {
                    	// si tiene un parámetro, convierte el "1" en numero
                        paginaActual = Integer.parseInt(request.getParameter("page"));
                        // si recibe un mensaje malicioso, evita que la pagina explote
                        // dejando la página en 0
                    } catch (NumberFormatException e) {
                        paginaActual = 0; 
                    }
                }
                
                // comprobación de seguridad para saber si el usuario
                // realmente ha pasado un alias a la URL
                // si no hay alias, no hace nada y muestra la pagina vacía
                if (aliasHistorial != null && !aliasHistorial.isBlank()) {
                	// Llama el método handleConsultarHistorial, trayendo solo 5
                    List<UsuarioPelicula> historial = cineService.handleConsultarHistorial(aliasHistorial, paginaActual, HISTORIAL_PAGE_SIZE);
                    // Mira cuantas películas tiene en total
                    int totalRegistros = cineService.getTotalHistorial(aliasHistorial);
                    // Logica paginación, calcula si debe dibujar el botón Siguiente
                    // a partir de 12 peliculas, se convierte en 2 paginas y aparece el botón de "siguiente"
                    // y la variable hayMas, se pone a true
                    boolean hayMas = (long) (paginaActual + 1) * HISTORIAL_PAGE_SIZE < totalRegistros;
                    
                    // prepara el request para el JSP. Mete todos los datos que el JSP necesita para pintarse
                    request.setAttribute("historial", historial);
                    request.setAttribute("alias", aliasHistorial);
                    request.setAttribute("paginaActual", paginaActual);
                    request.setAttribute("paginado", true);
                    request.setAttribute("hayMas", hayMas);
                }
                
                // una vez todo hecho, debe mostrarse el JSP de historial
                vistaJSP = "/WEB-INF/views/historial.jsp";
                break;

            case "TOP_PELICULAS":
                List<Pelicula> top = cineService.handleTopPeliculas(TOP_PELICULAS_LIMIT);
                request.setAttribute("topPeliculas", top);
                vistaJSP = "/WEB-INF/views/top.jsp";
                break;

            default:
                vistaJSP = "/WEB-INF/views/home.jsp";
        }

        // 3. (Forward) Despachar al JSP
        // * coge ese valor y le pasa la petición al archivo home.jsp para que se "pinte"
        // * CineController deja de trabajar en esa petición y le pasa la responsabilidad 
        // * total al home.jsp para que él termine el trabajo
        
        // * el servidor le pasa el control a home.jsp
        request.getRequestDispatcher(vistaJSP).forward(request, response);
    }


    //   doPOST: SOLO para PROCESAR ACCIONES (formularios)

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); 

        // 1. Recoger parámetros del formulario con el objeto request
        String op = request.getParameter("operacion");
        String alias = request.getParameter("alias");
        String genero = request.getParameter("genero");
        String valoracionStr = request.getParameter("valoracion");

        HttpSession session = request.getSession();
        String urlRedireccion = request.getContextPath() + "/cine?op=HOME"; 

        // --- INICIO PRG (Paso 1: Post) ---
        // Guardamos los datos del formulario en SESIÓN
        
        // Guardas esos datos en la Sesión para que no se borren 
        // durante el redirect y así poder volver a pintar el formulario 
        // con los datos que el usuario ya había escrito.
        session.setAttribute("aliasActual", alias);
        session.setAttribute("generoActual", genero);
        session.setAttribute("valoracionActual", valoracionStr);
        
        // 2. Switch para procesar la operación
        switch (op) {
            case "REGISTRAR":
            	try {
                    
                    
                    // 1. Primero creo la variable y la pongo a null por si acaso.
                    Double valoracion = null; 
                    
                    // 2. Ahora compruebo si el string del formulario NO está vacío.
                    if (valoracionStr != null && !valoracionStr.equals("")) {
                        
                        // 3. Si tiene algo escrito, lo convierto a número (Double).
                        valoracion = Double.parseDouble(valoracionStr);
                    }
                    // el controlador le pasa los datos al CineService y se espera a que le 
                    // devuelva el resultado
                    // CURRANTE
                    
                    // CineService habla con los DAO, guarda los datos en la BD y devuelve un mensaje
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
        // apunta a HOME
        response.sendRedirect(urlRedireccion);
    }

    /**
     * --- INICIO PRG (Paso 3: Get) ---
     * Mueve los datos de la Sesión (donde los puso doPost)
     * a la Request (para que los lea el JSP).
     */
    private void transferirDatosPRG(HttpServletRequest request) {
    	
        HttpSession session = request.getSession();

        // Encuentra "flash". Lo coge, lo pone en la Request 
        //(request.setAttribute...), y lo borra de la Sesión.
        
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