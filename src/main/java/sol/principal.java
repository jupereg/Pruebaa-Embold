package sol;

import model.ExcepcionDeAplicacion;

public class principal {
	public static void main(String[] args) {
		try {
		GestorBD g=new GestorBD();
		System.out.println(g.incrementarSueldoUpdatableResultSet(20));
		System.out.println(g.incrementarSueldo(20));		
		}
		catch(ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}
}
