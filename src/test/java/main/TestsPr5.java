package main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementTable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import model.Empleado;
import model.ExcepcionDeAplicacion;
import model.Finca;
import model.Propietario;
import model.Sucursal;
import sol.GestorBD;
import util.TestsUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestsPr5 extends TestsUtil {

	@BeforeClass
	public static void creacionGestorBD() {
		gbd = new GestorBD();
		url = GestorBD.getPropiedad("url");
		user = GestorBD.getPropiedad("user");
		password = GestorBD.getPropiedad("password");
		schema = GestorBD.getPropiedad("schema");
	}

	// Antes de ejecutar cada test, eliminamos el estado previo de la BD, eliminando
	// los registros insertados en el test previo y cargando los datos requeridos
	// para dicho test.
	@Before
	public void importDataSet() throws Exception {
		IDataSet dataSet = readDataSet();
		cleanlyInsertDataset(dataSet);
	}

///////////////////// Test de la Practica 4 ///////////////////////
///////////////////////////////////////////////////////////////////
	@Test
	public void testGetFinca() throws ExcepcionDeAplicacion {
		try {
			// Obtenemos de la BD la finca esperada
			Finca fincaObtenida = gbd.getFinca("HCp001");
			// Creamos un objeto con la Finca esperada
			Propietario prop = new Propietario("ElGC01", "Eladio", "Gutierrez Casado", "P. Constitucion 3",
					"976112233");
			Finca fincaEsperada = new Finca("HCp001", "C/ Hernan Cortes 1", "Zaragoza", "50005", "piso", new Integer(6),
					new Integer(2), "central", true, prop, new Double(300));
			// Comprobamos que coinciden
			assertEquals("Falla al comprobar la finca", fincaEsperada, fincaObtenida);
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetEmpleado() throws ExcepcionDeAplicacion {
		try {
			// Obtenemos de la BD el empleado esperado
			Empleado empleadoObtenido = gbd.getEmpleado("AGar01");
			// Creamos un objeto con el empleado esperado
			Sucursal suc = new Sucursal("BrZa01", "Breton 4", "Zaragoza", "50009");
			GregorianCalendar fechNacim = new GregorianCalendar(1966, 0, 1);
			Empleado empleadoEsperado = new Empleado("AGar01", "Alberto", "Garcia Romero", "director", 'h', 2035.0,
					fechNacim, suc);
			// Comprobamos que ambos empleados coinciden
			assertEquals("Falla al comprobar el empleado", empleadoEsperado, empleadoObtenido);
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBuscaFincas() {
		try {
			// Obtenemos de la BD el listado de las fincas cuyo percio de alquiler se
			// encuentra en el intervalo [100,900]
			List<Finca> resultadoObtenido = gbd.buscaFincas(100, 900);
			List<String> resultadoObtenidoString = new ArrayList<String>();
			for (Finca f : resultadoObtenido) {
				resultadoObtenidoString.add(f.getId());
			}
			// Cargo los identificadores de las fincas esperadas desde el XML
			// fincasEsperadas.xml
			List<String> resultadoEsperado = leerXML("src/fincasEsperadas.xml", "finca", "ID_FINCA");
			// Comprobamos que coinciden
			assertTrue("Falla al buscar fincas: el numero de registros no coincide",
					resultadoObtenidoString.size() == resultadoEsperado.size());
			assertTrue("Falla al buscar fincas: las fincas obtenidas no son las correctas",
					resultadoObtenidoString.containsAll(resultadoEsperado)
							&& resultadoEsperado.containsAll(resultadoObtenidoString));
		} catch (ExcepcionDeAplicacion ex) {
			fail("Error buscando" + ex);
			ex.printStackTrace();
		}
	}

	@Test
	public void testIncrementarSueldo() {
		try {
			// Incrementamos el sueldo un 20% invocando al metodo incrementarSueldo
			gbd.incrementarSueldo(20);
			// Obtenemos de la BD el contenido de la tabla Empleado tras la modificacion
			// del sueldo
			ITable tablaEmpleadoObtenida = getTablaActual(url, user, password, schema, "empleado");
			// Cargamos los datos esperados del XML
			ITable tablaEmpleadoEsperada = getTablaEsperada("empleado", "src/salariosEmpleadosEsperados.xml");
			// Comprobamos que el contenido actual de la tabla Empleado en la BD coincide
			// con la
			// tabla esperada cargada en el XML
			Assertion.assertEquals(tablaEmpleadoEsperada, tablaEmpleadoObtenida);
		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEliminarCliente() {
		try {
			// Invoco al metodo eliminarCliente con objeto de eliminar al cliente con
			// identificador 'RMaC01'
			gbd.eliminarCliente("RMaC01");
			// Obtengo de la BD el contenido de la tabla Cliente resultante tras la
			// eliminacion del cliente anterior
			ITable tablaClienteObtenida = getTablaActual(url, user, password, schema, "cliente");
			// Cargo los datos de los clientes esperados tras la eliminaciï¿½n del
			// 'RMaC01',
			// desde el XML clientesEsperados.xml
			ITable tablaClienteEsperada = getTablaEsperada("cliente", "src/clientesEsperados.xml");
			// Compruebo que el contenido actual de la tabla Cliente en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaClienteEsperada, tablaClienteObtenida);
		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

///////////////////// Test adicional para controlar nulos en Finca (Practica 4) ///////////////////////
///////////////////////////////////////////////////////////////////
	@Test
	public void testGetFincaNulos() throws ExcepcionDeAplicacion {
		try {
			// Obtenemos de la BD la finca esperada
			Finca fincaObtenida = gbd.getFinca("Tup002");
			// Creamos un objeto con la Finca esperada
			Propietario prop = new Propietario("ElGC01", "Eladio", "Gutierrez Casado", "P. Constitucion 3",
					"976112233");
			Finca fincaEsperada = new Finca("Tup002", "C/  Turco 20", "Zaragoza", "50002", "piso", new Integer(3), null,
					"si", true, prop, new Double(1000));
			// Comprobamos que coinciden
			assertEquals("Falla al comprobar la finca", fincaEsperada, fincaObtenida);
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

	///////////////////// Test de la Practica 5 ///////////////////////
	///////////////////////////////////////////////////////////////////
	@Test
	public void testEliminarEmpleados() {
		List<String> empleados = new ArrayList<String>();
		empleados.add("MaAl01");
		empleados.add("JuLo01");
		try {
			// Invoco al metodo eliminarEmpleados con objeto de eliminar los empleados de la
			// lsita anterior
			gbd.eliminarEmpleados(empleados);
			// Obtengo de la BD el contenido de la tabla Empleado resultante tras la
			// eliminacion de la lista de empleados
			ITable tablaEmpleadoObtenida = getTablaActual(url, user, password, schema, "empleado");
			// Cargo los datos de los empleados esperados tras la eliminacion de los dos
			// anteriores 'MaAl01' y 'JuLo01'
			// desde el XML empleadosEsperados.xml
			ITable tablaEmpleadoEsperada = getTablaEsperada("empleado", "src/empleadosEsperados1.xml");
			// Compruebo que el contenido actual de la tabla Empleado en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaEmpleadoEsperada, tablaEmpleadoObtenida);
		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testAniadirEmpleado1() {
		Empleado empleado1 = new Empleado("TOla01", "Tomas", "Olarte Antiguo", "comercial", 'h', new Double(1500),
				new java.util.GregorianCalendar(1968, 3, 1), new Sucursal("BrZa01", "Breton 4", "Zaragoza", "50009"));

		try {
			// Invoco al metodo aniadirEmpleado
			gbd.aniadirEmpleado(empleado1);
			// Compruebo las sucursales
			// Obtengo de la BD el contenido de la tabla Sucursal resultante tras aniadir el
			// empleado
			ITable tablaSucursalObtenida = getTablaActual(url, user, password, schema, "sucursal");
			// Desde el XML sucursalesEsperadas.xml, cargo los datos de las sucursales
			// esperadas tras aniadir el empleado anterior
			ITable tablaSucursalEsperada = getTablaEsperada("sucursal", "src/sucursalesEsperadas1.xml");
			// Compruebo que el contenido actual de la tabla Sucursal en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaSucursalEsperada, tablaSucursalObtenida);
			// Compruebo los empleados
			// Obtengo de la BD el contenido de la tabla Empleado resultante tras aniadir el
			// empleado
			ITable tablaEmpleadoObtenida = getTablaActual(url, user, password, schema, "empleado");
			// Desde el XML empleadosEsperados.xml, cargo los datos de los empleados
			// esperados tras aniadir
			// el empleado anterior
			ITable tablaEmpleadoEsperada = getTablaEsperada("empleado", "src/empleadosEsperados2.xml");
			// Compruebo que el contenido actual de la tabla Empleado en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaEmpleadoEsperada, tablaEmpleadoObtenida);

		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testAniadirEmpleado2() {
		Empleado empleado2 = new Empleado("Salv01", "Sonia", "Alvarez Garcia", "director", 'm', new Double(2300),
				new java.util.GregorianCalendar(1968, 2, 1),
				new Sucursal("JoLo01", "Jorge Vigon 68", "Zaragoza", "26004"));
		try {
			// Invoco al metodo aniadirEmpleado
			gbd.aniadirEmpleado(empleado2);
			// Compruebo las sucursales
			// Obtengo de la BD el contenido de la tabla Sucursal resultante tras aniadir el
			// empleado
			ITable tablaSucursalObtenida = getTablaActual(url, user, password, schema, "sucursal");
			// Desde el XML sucursalesEsperadas.xml, cargo los datos de las sucursales
			// esperadas tras aniadir el empleado anterior
			ITable tablaSucursalEsperada = getTablaEsperada("sucursal", "src/sucursalesEsperadas2.xml");
			// Compruebo que el contenido actual de la tabla Sucursal en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaSucursalEsperada, tablaSucursalObtenida);
			// Compruebo los empleados
			// Obtengo de la BD el contenido de la tabla Empleado resultante tras aniadir el
			// empleado
			ITable tablaEmpleadoObtenida = getTablaActual(url, user, password, schema, "empleado");
			// Desde el XML empleadosEsperados.xml, cargo los datos de los empleados
			// esperados tras aniadir
			// el empleado anterior
			ITable tablaEmpleadoEsperada = getTablaEsperada("empleado", "src/empleadosEsperados3.xml");
			// Compruebo que el contenido actual de la tabla Empleado en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaEmpleadoEsperada, tablaEmpleadoObtenida);

		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIncrementarSueldoUpdatableResultSet() {
		try {
			// Incrementamos el sueldo un 20% invocando al metodo incrementarSueldo
			gbd.incrementarSueldoUpdatableResultSet(20);
			// Obtenemos de la BD el contenido de la tabla Empleado tras la modificacion
			// del sueldo
			ITable tablaEmpleadoObtenida = getTablaActual(url, user, password, schema, "empleado");
			// Cargamos los datos esperados del XML
			ITable tablaEmpleadoEsperada = getTablaEsperada("empleado", "src/salariosEmpleadosEsperados.xml");
			// Comprobamos que el contenido actual de la tabla Empleado en la BD coincide
			// con la
			// tabla esperada cargada en el XML
			Assertion.assertEquals(tablaEmpleadoEsperada, tablaEmpleadoObtenida);
		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

///////////////////// Test del ejercicio opcional de la Practica 5 ///////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testAniadirCaptacion1() {
		try {
			// Creo el objeto de la Finca a captar
			Finca f = new Finca("f111", "Jorge Vigon 8", "Zaragoza", "26004", "unifamiliar", new Integer(5),
					new Integer(3), "individual", false, gbd.getPropietario("ElGC01"), new Double(1200));
			// Invoco al metodo aniadirCaptacion
			gbd.aniadirCaptacion(f, "AGar01");
			/////////////////////////////
			// Compruebo las captaciones//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Captacion resultante tras aniadir
			///////////////////////////// captacion anterior
			ITable tablaCaptacionObtenida = getTablaActual(url, user, password, schema, "captacion");
			// Cargo los datos de las captaciones esperadas tras aniadir la captacion
			// desde el XML captacionesEsperadasAv1.xml
			ITable tablaCaptacionEsperada = getTablaEsperada("captacion", "src/captacionesEsperadasAv1.xml");
			ReplacementTable tablaCaptacionEsperadaReemplazada = new ReplacementTable(tablaCaptacionEsperada);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaHoy = new Date(System.currentTimeMillis());
			tablaCaptacionEsperadaReemplazada.addReplacementObject("[TODAY]", sdf.format(fechaHoy));
			// Compruebo que el contenido actual de la tabla Captacion en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaCaptacionEsperadaReemplazada, tablaCaptacionObtenida);
			/////////////////////////////
			// Compruebo las comisiones//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Comision resultante tras aniadir
			// captacion anterior
			ITable tablaComisionObtenida = getTablaActual(url, user, password, schema, "comision");
			// Cargo los datos de las comisiones esperadas tras aniadir la captacion
			// desde el XML comisionesEsperadasAv1.xml
			ITable tablaComisionEsperada = getTablaEsperada("comision", "src/comisionesEsperadasAv1.xml");
			ReplacementTable tablaComisionEsperadaReemplazada = new ReplacementTable(tablaComisionEsperada);
			tablaComisionEsperadaReemplazada.addReplacementObject("[TODAY]", sdf.format(fechaHoy));
			// Compruebo que el contenido actual de la tabla Comision en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaComisionEsperadaReemplazada, tablaComisionObtenida);
			/////////////////////////////
			// Compruebo las fincas//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Finca resultante tras aniadir la
			// captacion anterior
			ITable tablaFincaObtenida = getTablaActual(url, user, password, schema, "finca");
			ReplacementTable tablaFincaObtenidaReemplazada = new ReplacementTable(tablaFincaObtenida);
			tablaFincaObtenidaReemplazada.addReplacementObject("[null]", null);
			// Cargo los datos de las fincas esperadas tras aniadir la captacion
			// desde el XML fincasEsperadasAv1.xml
			ITable tablaFincaEsperada = getTablaEsperada("finca", "src/fincasEsperadasAv1.xml");
			ReplacementTable tablaFincaEsperadaReemplazada = new ReplacementTable(tablaFincaEsperada);
			tablaFincaEsperadaReemplazada.addReplacementObject("[null]", null);
			// Compruebo que el contenido actual de la tabla Finca en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaFincaEsperadaReemplazada, tablaFincaObtenidaReemplazada);

		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAniadirCaptacion2() {
		try {
			// Creo el objeto de la Finca a captar
			Finca f = new Finca("f222", "Gran Via 8", "Zaragoza", "26002", "piso", new Integer(2), new Integer(1),
					"individual", false, gbd.getPropietario("AnRo01"), new Double(700));
			// Invoco al metodo aniadirCaptacion
			gbd.aniadirCaptacion(f, "ADom01");
			/////////////////////////////
			// Compruebo las captaciones//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Captacion resultante tras aniadir
			///////////////////////////// captacion anterior
			ITable tablaCaptacionObtenida = getTablaActual(url, user, password, schema, "captacion");
			// Cargo los datos de las captaciones esperadas tras aniadir la captacion
			// desde el XML captacionesEsperadasAv2.xml
			ITable tablaCaptacionEsperada = getTablaEsperada("captacion", "src/captacionesEsperadasAv2.xml");
			ReplacementTable tablaCaptacionEsperadaReemplazada = new ReplacementTable(tablaCaptacionEsperada);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaHoy = new Date(System.currentTimeMillis());
			tablaCaptacionEsperadaReemplazada.addReplacementObject("[TODAY]", sdf.format(fechaHoy));
			// Compruebo que el contenido actual de la tabla Captacion en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaCaptacionEsperadaReemplazada, tablaCaptacionObtenida);
			/////////////////////////////
			// Compruebo las comisiones//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Comision resultante tras aniadir
			// captacion anterior
			ITable tablaComisionObtenida = getTablaActual(url, user, password, schema, "comision");
			// Cargo los datos de las comisiones esperadas tras aniadir la captacion
			// desde el XML comisionesEsperadasAv2.xml
			ITable tablaComisionEsperada = getTablaEsperada("comision", "src/comisionesEsperadasAv2.xml");
			ReplacementTable tablaComisionEsperadaReemplazada = new ReplacementTable(tablaComisionEsperada);
			tablaComisionEsperadaReemplazada.addReplacementObject("[TODAY]", sdf.format(fechaHoy));
			// Compruebo que el contenido actual de la tabla Comision en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaComisionEsperadaReemplazada, tablaComisionObtenida);
			/////////////////////////////
			// Compruebo las fincas//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Finca resultante tras aniadir la
			// captacion anterior
			ITable tablaFincaObtenida = getTablaActual(url, user, password, schema, "finca");
			ReplacementTable tablaFincaObtenidaReemplazada = new ReplacementTable(tablaFincaObtenida);
			tablaFincaObtenidaReemplazada.addReplacementObject("[null]", null);
			// Cargo los datos de las fincas esperadas tras aniadir la captacion
			// desde el XML fincasEsperadasAv2.xml
			ITable tablaFincaEsperada = getTablaEsperada("finca", "src/fincasEsperadasAv2.xml");
			ReplacementTable tablaFincaEsperadaReemplazada = new ReplacementTable(tablaFincaEsperada);
			tablaFincaEsperadaReemplazada.addReplacementObject("[null]", null);
			// Compruebo que el contenido actual de la tabla Finca en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaFincaEsperadaReemplazada, tablaFincaObtenidaReemplazada);

		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAniadirCaptacion3() {
		try {
			// Creo el objeto de la Finca a captar
			Finca f = new Finca("f333", "Eliseo Pinedo 1", "Zaragoza", "26004", "piso", new Integer(3), new Integer(1),
					"individual", false, new Propietario("P234", "Pedro", "Martinez", "Lardero 7", "343234421"),
					new Double(750));
			// Invoco al metodo aniadirCaptacion
			gbd.aniadirCaptacion(f, "MaAl01");
			/////////////////////////////
			// Compruebo las captaciones//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Captacion resultante tras aniadir
			///////////////////////////// captacion anterior
			ITable tablaCaptacionObtenida = getTablaActual(url, user, password, schema, "captacion");
			// Cargo los datos de las captaciones esperadas tras aniadir la captacion
			// desde el XML captacionesEsperadasAv3.xml
			ITable tablaCaptacionEsperada = getTablaEsperada("captacion", "src/captacionesEsperadasAv3.xml");
			ReplacementTable tablaCaptacionEsperadaReemplazada = new ReplacementTable(tablaCaptacionEsperada);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaHoy = new Date(System.currentTimeMillis());
			tablaCaptacionEsperadaReemplazada.addReplacementObject("[TODAY]", sdf.format(fechaHoy));
			// Compruebo que el contenido actual de la tabla Captacion en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaCaptacionEsperadaReemplazada, tablaCaptacionObtenida);
			/////////////////////////////
			// Compruebo las comisiones//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Comision resultante tras aniadir
			// captacion anterior
			ITable tablaComisionObtenida = getTablaActual(url, user, password, schema, "comision");
			// Cargo los datos de las comisiones esperadas tras aniadir la captacion
			// desde el XML comisionesEsperadasAv3.xml
			ITable tablaComisionEsperada = getTablaEsperada("comision", "src/comisionesEsperadasAv3.xml");
			ReplacementTable tablaComisionEsperadaReemplazada = new ReplacementTable(tablaComisionEsperada);
			tablaComisionEsperadaReemplazada.addReplacementObject("[TODAY]", sdf.format(fechaHoy));
			// Compruebo que el contenido actual de la tabla Comision en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaComisionEsperadaReemplazada, tablaComisionObtenida);
			/////////////////////////////
			// Compruebo las fincas//
			/////////////////////////////
			// Obtengo de la BD el contenido de la tabla Finca resultante tras aniadir la
			// captacion anterior
			ITable tablaFincaObtenida = getTablaActual(url, user, password, schema, "finca");
			ReplacementTable tablaFincaObtenidaReemplazada = new ReplacementTable(tablaFincaObtenida);
			tablaFincaObtenidaReemplazada.addReplacementObject("[null]", null);
			// Cargo los datos de las fincas esperadas tras aniadir la captacion
			// desde el XML fincasEsperadasAv3.xml
			ITable tablaFincaEsperada = getTablaEsperada("finca", "src/fincasEsperadasAv3.xml");
			ReplacementTable tablaFincaEsperadaReemplazada = new ReplacementTable(tablaFincaEsperada);
			tablaFincaEsperadaReemplazada.addReplacementObject("[null]", null);
			// Compruebo que el contenido actual de la tabla Finca en la BD coincide con
			// la informacion esperada cargada del XML
			Assertion.assertEquals(tablaFincaEsperadaReemplazada, tablaFincaObtenidaReemplazada);

		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		} catch (ExcepcionDeAplicacion e) {
			e.printStackTrace();
		}
	}

}
