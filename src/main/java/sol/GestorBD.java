package sol;
	//Juan Pérez Garrido
	//Correccion:Ismael Cariñanos (comentarios en los que pone ISMA)
	import java.io.IOException;
	import java.io.InputStream;
	import java.sql.Connection;
	import java.sql.Date;
	import java.sql.DriverManager;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
	import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
	import java.util.Calendar;
	import java.util.GregorianCalendar;
	import java.util.List;
	import java.util.Properties;

	import model.Empleado;
	import model.ExcepcionDeAplicacion;
	import model.Finca;
	import model.Propietario;
	import model.Sucursal;
	import pers.Persistencia;

	public class GestorBD implements Persistencia {

		//Constantes
		private static final String URL = getPropiedad("url");
		private static final String USR = getPropiedad("user");
		private static final String PWD = getPropiedad("password");
		//Métodos
		public static String getPropiedad(String clave) {
		String valor = null;
		try {
		    Properties props = new Properties();
		    InputStream prIS = GestorBD.class.getResourceAsStream("/conexion.properties");
		    props.load(prIS);
		    valor = props.getProperty(clave);
		} catch (IOException ex) { ex.printStackTrace();
		}
		return valor;
		}
		@Override
		public List<Finca> buscaFincas(double arg0, double arg1) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			List<Finca> lista=new ArrayList<>();
			Connection con=null;
			Finca finca=null;
			try {
				//Vamos a usar PreparedStatement
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="SELECT * FROM finca WHERE alquiler>=? AND alquiler<=?";
				PreparedStatement ps=con.prepareStatement(s1);//consulta ya precompilada
				ps.setDouble(1, arg0);//primera componente ? de la consulta
				ps.setDouble(2, arg1);//segunda componente ? de la consulta
				ResultSet rs=ps.executeQuery();//se realiza y almacena la consulta
				
				while(rs.next()) {
					//nos ayudamos del método creado previamente
					lista.add(getFinca(rs.getString(1)));//No hace falta el this.
				}
				//cerramos conexiones
				rs.close();
				ps.close();
				
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
					try {
						if(con!=null)
							con.close();
					}
					catch(SQLException e) {
						e.printStackTrace();
						throw new ExcepcionDeAplicacion(e);
					}
				}
			return lista;
		}

		@Override
		public boolean eliminarCliente(String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			boolean b;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				String s1="Delete from cliente where id_cliente=?";
				String s2="Delete from visita where id_cliente=?";
				PreparedStatement ps1=con.prepareStatement(s1);
				PreparedStatement ps2=con.prepareStatement(s2);
				ps1.setString(1, arg0);
				ps2.setString(1, arg0);
				//Primero borrar de las visitas porque al revés no funcionaría
				ps2.executeUpdate();
				int cantidad=ps1.executeUpdate();//Con comprobar solo en la tabla cliente sabremos si se ha hecho algún cambio
				
				b=false;
				if(cantidad!=0)
					b=true;
				
				ps1.close();
				ps2.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			return b;
		}

		@Override
		public Empleado getEmpleado(String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			//Realizado con join
			Empleado empleado=null;
			Sucursal sucursal=null;
			Connection con=null;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="SELECT * FROM empleado WHERE id_empleado=?";
				String s2="SELECT s.* FROM sucursal s JOIN empleado e ON s.id_sucursal=e.sucursal WHERE e.id_empleado=?";
				PreparedStatement ps1=con.prepareStatement(s1);//consulta ya precompilada
				PreparedStatement ps2=con.prepareStatement(s2);//consulta ya precompilada
				ps1.setString(1, arg0);//primera componente ? de la primera consulta
				ps2.setString(1, arg0);//primera componente ? de la segunda consulta
				ResultSet rs1=ps1.executeQuery();//ejecutamos y almacenamos la consulta
				ResultSet rs2=ps2.executeQuery();//ejecutamos y almacenamos la consulta						
				
				if(rs2.next()) {
					sucursal=new Sucursal(rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4));
				}
				if(rs1.next()) {
					//hay un fallo en el javaDoc, calendar en la BD es la posicion 6, pero en el constructor la 7
					char c=rs1.getString(5).charAt(0);//obtenemos el primer caracter de la String. Se podría
														//hacer en la misma línea de creación de empleado
//ISMA					//Comprobar primero si es o no null. por ej -> char c=(rs1.getString(5)==null)? null:rs1.charAt(0)
					
					Double salario=rs1.getDouble(7);
					if(rs1.wasNull()) salario=null;
					
//ISMA				Calendar cal=new GregorianCalendar(); //Convertir el date a calendar.por ejemplo -> Date d = rs.getDate(6);
																									//Calendar f = Calendar.getInstance();
																									//f.setTime(d);
					//cal.setTime(rs1.getDate(6));		
					Date d=rs1.getDate(6);
					Calendar cal=Calendar.getInstance();
					cal.setTime(d);
					
					empleado=new Empleado(rs1.getString(1),rs1.getString(2),rs1.getString(3),rs1.getString(4),c,salario,cal,sucursal);
				}
				//cerramos conexiones
				rs1.close();rs2.close();
				ps1.close();ps2.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
					try {
						if(con!=null)
							con.close();
					}
					catch(SQLException e) {
						e.printStackTrace();
						throw new ExcepcionDeAplicacion(e);
					}
				}
			return empleado;
		}

		@Override
		public Finca getFinca(String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			//Lo realizaremos con un JOIN
			Finca finca=null;
			Propietario prop=null;
			Connection con=null;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="SELECT * FROM finca WHERE id_finca=?";
				String s2="SELECT P.* FROM PROPIETARIO AS P JOIN FINCA AS F ON P.ID_PROPIETARIO=F.PROPIETARIO WHERE id_finca =?";	
				PreparedStatement pstm = con.prepareStatement(s1);//consulta ya precompilada
				PreparedStatement pstm2=con.prepareStatement(s2);//consulta ya precompilada
				pstm.setString(1, arg0);//primera componente ? de la primera consulta
				pstm2.setString(1, arg0);//primera componente ? de la segunda consulta
				ResultSet rs1=pstm.executeQuery();//ejecutamos y almacenamos la consulta
				ResultSet rs2=pstm2.executeQuery();	//ejecutamos y almacenamos la consulta
				
				//Boolean ascB=(asc==null)? null: asc.equalsIgnoreCase("Si")
				
				if(rs2.next()) {
					prop=new Propietario(rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4),rs2.getString(5));
				}			
				if(rs1.next()) {
					  String s="si";
			          boolean ascensor=true;
//ISMA			      if(s.compareTo(rs1.getString(9))!=0)//Mejor usar equalsIgnoreCase ->(!rs1.getString(9).equalsIgnoreCase(s))
//			          {
//			        	  ascensor=false;
//			          }
			          if(!rs1.getString(9).equalsIgnoreCase(s))
			        	  ascensor=false;
			          
//			         Boolean ascensor=rs1.getBoolean(9);
//			         if(rs1.wasNull()) ascensor=null; No funciona, en la BD original es "si" o "no"
			         Integer habitaciones = rs1.getInt(6); 
				  	 if (rs1.wasNull()) habitaciones=null;
			         Integer banios = rs1.getInt(7);
			  		 if (rs1.wasNull()) banios=null;
			  		 Double alquiler = rs1.getDouble(10); 
			  		 if (rs1.wasNull()) alquiler=null;
			  		 
			  		 finca=new Finca(rs1.getString(1),rs1.getString(2),rs1.getString(3),rs1.getString(4),rs1.getString(5),habitaciones,banios,rs1.getString(8),ascensor,prop,alquiler);
			  		
				}
				//cerramos las conexiones
				rs1.close();rs2.close();
				pstm.close();pstm2.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
					try {
						if(con!=null)
							con.close();
					}
					catch(SQLException e) {
						e.printStackTrace();
						throw new ExcepcionDeAplicacion(e);
					}
				}
			
			return finca;
		}

		@Override
		public int incrementarSueldo(float arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			int resultado=0;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="UPDATE empleado SET salario=salario+(salario*?/100)";
				PreparedStatement ps=con.prepareStatement(s1);
				ps.setFloat(1,arg0);
				resultado=ps.executeUpdate();
				
				ps.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			return resultado;
		}
		
		//PRÁCTICA 5
		@Override
		public void aniadirEmpleado(Empleado arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			Sucursal sucursal=arg0.getSucursal();
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				
				if(this.getEmpleado(arg0.getId())==null) { 
					String sql="SELECT * FROM sucursal WHERE id_sucursal=?";					
					PreparedStatement ps=con.prepareStatement(sql);
					ps.setString(1, sql);
					ResultSet rs=ps.executeQuery();
					if(!rs.next()) {
						String sql2="INSERT INTO sucursal VALUES(?,?,?,?)";
						PreparedStatement ps2=con.prepareStatement(sql2);
						ps2.setString(1, sucursal.getId());	
						ps2.setString(2, sucursal.getDireccion());		
						ps2.setString(3, sucursal.getCiudad());		
						ps2.setString(4, sucursal.getCP());	
						ps2.executeUpdate();
						
						ps2.close();						
					}
					rs.close();
					ps.close();					
					
					String sql3="INSERT INTO empleado VALUES(?,?,?,?,?,?,?,?)";
					PreparedStatement ps3=con.prepareStatement(sql3);
					ps3.setString(1, arg0.getId());
					ps3.setString(2, arg0.getNombre());
					ps3.setString(3, arg0.getApellidos());
					ps3.setString(4, arg0.getTrabajo());
					if(arg0.getSexo()==null)
						ps3.setNull(5, Types.BOOLEAN);
					else
						ps3.setString(5, Character.toString(arg0.getSexo()));
					Calendar fechaN=arg0.getFechaNacimiento();
					if(fechaN==null)
						ps3.setDate(6, null);
					else
						ps3.setDate(6, new java.sql.Date(fechaN.getTimeInMillis()));
						
					if(arg0.getSalario()==null)
						ps3.setNull(7, Types.DOUBLE);
					else
						ps3.setDouble(7, arg0.getSalario());
					
					ps3.setString(8,arg0.getSucursal().getId() );
					
					ps3.executeUpdate();
					
					ps3.close();
					con.commit();//cierre de transacción
				}
			}
			catch(SQLException e) {
				e.printStackTrace();	
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();	
					throw new ExcepcionDeAplicacion(e);
				}
			}
			
		}
		@Override
		public int eliminarEmpleados(List<String> arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			int resultado=0;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				String s1="DELETE FROM visita WHERE id_empleado=?";
				String s2="DELETE FROM captacion WHERE id_empleado=?";
				String s3="DELETE FROM empleado WHERE id_empleado=?";
				PreparedStatement ps1=con.prepareStatement(s1);
				PreparedStatement ps2=con.prepareStatement(s2);
				PreparedStatement ps3=con.prepareStatement(s3);
				for(int i=0;i<arg0.size();i++) {
					ps1.setString(1, arg0.get(i));
					ps1.executeUpdate();
					ps2.setString(1, arg0.get(i));
					ps2.executeUpdate();
					ps3.setString(1, arg0.get(i));
					
					resultado=resultado + ps3.executeUpdate();
				}
				ps1.close();ps2.close();ps3.close();
				con.commit();
			}
			catch(SQLException e) {
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			return resultado;
		}
		@Override
		public int incrementarSueldoUpdatableResultSet(float arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			return 0;
		}

}

