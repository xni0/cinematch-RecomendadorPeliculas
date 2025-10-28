package entidades;

public class Pelicula {

	private int id;
	private String titulo;
	private String genero;
	private double puntuacionMedia; 
	

	public Pelicula() {
	}
	

	public Pelicula(int id, String titulo, String genero, double puntuacionMedia) {
		this.id = id;
		this.titulo = titulo;
		this.genero = genero;
		this.puntuacionMedia = puntuacionMedia;		
	}


	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String string) {
		this.genero = string;
	}


	public double getPuntuacionMedia() {
		return puntuacionMedia;
	}


	public void setPuntuacionMedia(double puntuacionMedia) {
		this.puntuacionMedia = puntuacionMedia;
	}
}