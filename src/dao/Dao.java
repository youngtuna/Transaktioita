package dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import bean.*;

public class Dao {

	private Connection yhdista() throws SQLException {
		Connection tietokantayhteys = null;

		String JDBCAjuri = "org.mariadb.jdbc.Driver";
		String url = "jdbc:mariadb://localhost:3306/a1403030";

		try {
			Class.forName(JDBCAjuri).newInstance(); // ajurin määritys

			// otetaan yhteys tietokantaan
			tietokantayhteys = DriverManager.getConnection(url, "a1403030",
					"niQY3372b");

			// yhteyden otto onnistu
		} catch (SQLException sqlE) {
			System.err.println("Tietokantayhteyden avaaminen ei onnistunut. "
					+ url + "\n" + sqlE.getMessage() + " " + sqlE.toString()
					+ "\n");
			throw (sqlE);
		} catch (Exception e) {
			System.err.println("TIETOKANTALIITTYN VIRHETILANNE: "
					+ "JDBC:n omaa tietokanta-ajuria ei loydy.\n\n"
					+ e.getMessage() + " " + e.toString() + "\n");
			e.printStackTrace();
			System.out.print("\n");
			throw (new SQLException("Tietokanta-ajuria ei loydy!"));
		}
		return tietokantayhteys;
	}
	
	public ArrayList<Nide> haeNiteet() throws SQLException, Exception {
		ArrayList<Nide> niteet = null;
		Kirja kirja = null;
		Nide nide = null;
		Connection conn = null;
		String sql = "SELECT isbn, nimi, kirjoittaja, painos, kustantaja,"
				+ "nidenro FROM kirja NATURAL JOIN nide WHERE NOT EXISTS"
				+ "(SELECT * FROM niteenlainaus WHERE nideisbn"
                + "=isbn AND niteenlainaus.nidenro=nide.nidenro AND palautuspvm is NULL)";
		PreparedStatement preparedStatement = null;
		ResultSet tulosjoukko = null;
		
		try {
			conn = yhdista();
			
			if (conn != null) {
				conn.setAutoCommit(false);
				/*
				 * level - one of the following Connection constants:
				 * Connection.TRANSACTION_READ_UNCOMMITTED ,
				 * Connection.TRANSACTION_READ_COMMITTED ,
				 * Connection.TRANSACTION_REPEATABLE_READ or
				 * Connection.TRANSACTION_SERIALIZABLE.
				 */
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				preparedStatement = conn.prepareStatement(sql);
				tulosjoukko = preparedStatement.executeQuery();
				
				if (tulosjoukko != null) {
					conn.commit();
					conn.close();
					
					while(tulosjoukko.next()) {
						String isbn = tulosjoukko.getString("isbn");
						String nimi = tulosjoukko.getString("nimi");
						String kirjoittaja = tulosjoukko.getString("kirjoittaja");
						int painos = tulosjoukko.getInt("painos");
						String kustantaja = tulosjoukko.getString("kustantaja");
						int nidenro = tulosjoukko.getInt("nidenro");
						
						kirja = new Kirja(isbn, nimi, kirjoittaja, painos, kustantaja);
						nide = new Nide (kirja, nidenro);
						
						if (niteet == null)
							niteet = new ArrayList<Nide>();
						niteet.add(nide);
					}
					tulosjoukko.close();
				} else {
					niteet = null;
					conn.commit();
					conn.close();
				}		
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (conn != null && conn.isClosed() == false) {
				try {
					conn.rollback();
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw new SQLException();
				}
			}
		}
		return niteet;
	}
	
	// Hakee kaikki asiakkaat
	public ArrayList<Asiakas> haeAsiakkaat() throws SQLException, Exception {
		ArrayList<Asiakas> asiakkaat = null;
		Asiakas asiakas = null;
		PostinumeroAlue posti = null;
		Connection conn = null;
		String sql = "SELECT * FROM asiakas NATURAL JOIN postinumeroalue";
		PreparedStatement preparedStatement = null; // suoritettava SQL-lause
		ResultSet tulosjoukko = null; // SQL-kyselyn tulokset
		
		try {
			conn = yhdista();
		 if (conn != null) {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			preparedStatement = conn.prepareStatement(sql);
			tulosjoukko = preparedStatement.executeQuery();
			
			if (tulosjoukko != null) {
				conn.commit();
				conn.close();
				
				while (tulosjoukko.next()) {
					int numero = tulosjoukko.getInt("numero");
					String etunimi = tulosjoukko.getString("etunimi");
					String sukunimi = tulosjoukko.getString("sukunimi");
					String osoite = tulosjoukko.getString("osoite");
					String postinro = tulosjoukko.getString("postinro");
					String postitmp = tulosjoukko.getString("postitmp");
					posti = new PostinumeroAlue(postinro, postitmp);
					asiakas = new Asiakas(numero, etunimi, sukunimi, osoite, posti);
					
					if (asiakkaat == null)
						asiakkaat = new ArrayList<Asiakas>();
					asiakkaat.add(asiakas);
				}
				tulosjoukko.close();
			} else {
				asiakkaat = null;
				conn.commit();
				conn.close();
			}
		 }
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (conn != null && conn.isClosed() == false) {
				try {
					conn.rollback();
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw new SQLException();
				}
			}
		}
		
		return asiakkaat;
	}
	
	public Lainaus haeLainaus(int numero) throws SQLException {
		String sql = "SELECT l.numero AS lainausnumero, lainauspvm, a.numero AS asiakasnumero, "
				+ "etunimi, sukunimi, osoite, p.postinro AS postinro, postitmp, "
				+ "k.isbn AS isbn, nimi, n.nidenro AS nidenro, "
				+ "palautuspvm,kirjoittaja, painos, kustantaja "
				+ "FROM lainaus l JOIN asiakas a ON l.lainaaja = a.numero "
				+ "JOIN postinumeroalue p ON p.postinro=a.postinro "
				+ "JOIN niteenlainaus nl ON l.numero = nl.lainausnro  "
				+ "JOIN nide n ON n.isbn = nl.nideisbn AND n.nidenro = nl.nidenro "
				+ "JOIN kirja k ON k.isbn = n.isbn   " + "WHERE l.numero =?";
		PreparedStatement preparedStatement = null; // suoritettava SQL-lause
		ResultSet tulosjoukko = null; // SQL-kyselyn tulokset
		Connection conn = null;
		Lainaus lainaus = null;
		Asiakas asiakas;
		NiteenLainaus niteenlainaus;

		try {
			conn = yhdista();
			if (conn != null) {
				conn.setAutoCommit(false);
				/*
				 * level - one of the following Connection constants:
				 * Connection.TRANSACTION_READ_UNCOMMITTED ,
				 * Connection.TRANSACTION_READ_COMMITTED ,
				 * Connection.TRANSACTION_REPEATABLE_READ or
				 * Connection.TRANSACTION_SERIALIZABLE.
				 */
				// eristyvyystason määritys
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				preparedStatement = conn.prepareStatement(sql);
				preparedStatement.setInt(1, numero);
				tulosjoukko = preparedStatement.executeQuery();
				preparedStatement.close();

				if (tulosjoukko != null && tulosjoukko.next()) {
					conn.commit(); // lopeta transaktio hyväksymällä
					conn.close(); // sulje yhteys kantaan heti

					lainaus = teeLainaus(tulosjoukko);

					asiakas = teeAsiakas(tulosjoukko);

					lainaus.setLainaaja(asiakas);

					niteenlainaus = teeNiteenLainaus(tulosjoukko);
					lainaus.addNiteenLainaus(niteenlainaus);
					while (tulosjoukko.next()) {
						niteenlainaus = teeNiteenLainaus(tulosjoukko);
						lainaus.addNiteenLainaus(niteenlainaus);
					}
					// System.out.println(lainaus);
					tulosjoukko.close();
				} else // tilausta ei löytynyt
				{
					lainaus = null;
					conn.commit();
					conn.close();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (conn != null && conn.isClosed() == false) {
				try {
					conn.rollback(); // peruuta transaktio
					conn.close(); // yhteys poikki
				} catch (Exception e) {
					e.printStackTrace();
					throw new SQLException();
				}
			}
		}
		return lainaus;
	}

	private Lainaus teeLainaus(ResultSet tulosjoukko) throws SQLException {
		Lainaus lainaus = null;
		int numero;
		Date pvm;

		if (tulosjoukko != null) {
			try {
				// System.out.println(tulosjoukko.getInt("lainausnumero") + " "
				// + tulosjoukko.getString("lainauspvm"));
				numero = tulosjoukko.getInt("lainausnumero");
				pvm = tulosjoukko.getDate("lainauspvm");

				lainaus = new Lainaus(numero, pvm);
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			}
		}

		return lainaus;
	}

	private Asiakas teeAsiakas(ResultSet tulosjoukko) throws SQLException {
		Asiakas asiakas = null;
		int numero;
		String etunimi;
		String sukunimi, osoite;
		String postinro;
		String postitmp;
		PostinumeroAlue posti = null;

		if (tulosjoukko != null) {
			try {
				numero = tulosjoukko.getInt("asiakasnumero");
				etunimi = tulosjoukko.getString("etunimi");
				sukunimi = tulosjoukko.getString("sukunimi");
				osoite = tulosjoukko.getString("osoite");
				postinro = tulosjoukko.getString("postinro");
				postitmp = tulosjoukko.getString("postitmp");
				posti = new PostinumeroAlue(postinro, postitmp);
				asiakas = new Asiakas(numero, etunimi, sukunimi, osoite, posti);
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			}
		}

		return asiakas;
	}

	private NiteenLainaus teeNiteenLainaus(ResultSet tulosjoukko)
			throws SQLException {
		NiteenLainaus rivi = null;
		Kirja kirja = null;
		Nide nide = null;
		String nimi, kirjoittaja, kustantaja, isbn;
		int painos, nidenro;
		Date pvm = null;
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

		if (tulosjoukko != null) {
			try {
				isbn = tulosjoukko.getString("isbn");
				// System.out.println(isbn);
				nimi = tulosjoukko.getString("nimi");
				// System.out.println(nimi);
				kirjoittaja = tulosjoukko.getString("kirjoittaja");
				// System.out.println(kirjoittaja);
				painos = tulosjoukko.getInt("painos");
				// System.out.println("" + painos);
				kustantaja = tulosjoukko.getString("kustantaja");
				// System.out.println(kustantaja);
				nidenro = tulosjoukko.getInt("nidenro");
				// System.out.println(nidenro);
				kirja = new Kirja(isbn, nimi, kirjoittaja, painos, kustantaja);
				// System.out.println(kirja);
				nide = new Nide(kirja, nidenro);
				// System.out.println(nide);

				pvm = tulosjoukko.getDate("palautuspvm");

				rivi = new NiteenLainaus(nide, pvm);
				// System.out.println(rivi);

			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			}
		}

		return rivi;

	}

	/**
	 * Hakee kannasta tilitaulun sisällön ArrayList-tyyppiseen kokoelmaan
	 * yhdessä transaktiossa.
	 *
	 * @return kaikki kannan sisältämät tilit yhdessä kokoelmassa
	 * @throws OdottamatonTietokantaPoikkeus
	 *             jos tapahtuu tietokantavirhe
	 * @throws KannassaEiTilejaPoikkeus
	 *             jos kannasta ei löydy vähintään kahta tiliä.
	 */
	public ArrayList<Lainaus> haeLainaukset() throws SQLException {

		ArrayList<Lainaus> lista = null;
		Connection conn = null;
		String sql = "SELECT l.numero as lainausnumero, lainauspvm, a.numero as asiakasnumero, "
				+ "etunimi, sukunimi, osoite, p.postinro AS postinro, postitmp, "
				+ "k.isbn AS isbn, nimi, n.nidenro AS nidenro, "
				+ "palautuspvm,kirjoittaja, painos, kustantaja "
				+ "FROM lainaus l JOIN asiakas a ON l.lainaaja = a.numero "
				+ "JOIN postinumeroalue p ON p.postinro=a.postinro "
				+ "JOIN niteenlainaus nl ON l.numero = nl.lainausnro  "
				+ "JOIN nide n ON n.isbn = nl.nideisbn AND n.nidenro = nl.nidenro "
				+ "JOIN kirja k ON k.isbn = n.isbn " + "ORDER BY lainausnumero";
		PreparedStatement preparedStatement = null; // suoritettava SQL-lause
		ResultSet tulosjoukko = null; // SQL-kyselyn tulokset
		Lainaus lainaus = null;
		Asiakas asiakas;
		NiteenLainaus niteenlainaus;
		boolean jatkuu = false;
		int lainausnumero;

		try {
			conn = yhdista();
			if (conn != null) {
				conn.setAutoCommit(false);
				/*
				 * level - one of the following Connection constants:
				 * Connection.TRANSACTION_READ_UNCOMMITTED ,
				 * Connection.TRANSACTION_READ_COMMITTED ,
				 * Connection.TRANSACTION_REPEATABLE_READ or
				 * Connection.TRANSACTION_SERIALIZABLE.
				 */
				// eristyvyystason määritys
				conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
				preparedStatement = conn.prepareStatement(sql);

				tulosjoukko = preparedStatement.executeQuery();
				preparedStatement.close();

				if (tulosjoukko != null) {
					conn.commit(); // lopeta transaktio hyväksymällä
					conn.close(); // sulje yhteys kantaan

					jatkuu = tulosjoukko.next();
					while (jatkuu) {
						lainaus = teeLainaus(tulosjoukko);
						System.out.println(lainaus);
						lainausnumero = lainaus.getNumero(); // ota
																// lainausnumero
																// talteen
						asiakas = teeAsiakas(tulosjoukko);
						lainaus.setLainaaja(asiakas);
						System.out.println(lainaus.getLainaaja());
						niteenlainaus = teeNiteenLainaus(tulosjoukko); // tee 1.
																		// niteenlainaus
						lainaus.addNiteenLainaus(niteenlainaus); // vie se
																	// lainaukselle
						System.out.println(niteenlainaus);
						if (lista == null)
							lista = new ArrayList<Lainaus>();
						lista.add(lainaus); // vie lainaus listaan

						jatkuu = tulosjoukko.next();
						while (jatkuu
								&& tulosjoukko.getInt("lainausnumero") == lainausnumero) // VAIHTUUKO
																							// lainaus?
						{
							niteenlainaus = teeNiteenLainaus(tulosjoukko);
							lainaus.addNiteenLainaus(niteenlainaus);
							System.out.println(niteenlainaus);
							jatkuu = tulosjoukko.next();
						}
					}
					tulosjoukko.close();

				} else // lainauksia ei löytynyt
				{
					lista = null;
					conn.commit();
					conn.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (conn != null && conn.isClosed() == false) {
				try {
					conn.rollback(); // peruuta transaktio
					conn.close(); // yhteys poikki
				} catch (Exception e) {
					e.printStackTrace();
					throw new SQLException();
				}
			}
		}

		return lista;

	}

	public ArrayList<Integer> haeLainausNumerot() throws SQLException,
			Exception {
		ArrayList<Integer> lista = null;
		Connection conn = null;
		String sql = "SELECT numero FROM lainaus ORDER BY numero";
		PreparedStatement preparedStatement = null; // suoritettava SQL-lause
		ResultSet tulosjoukko = null; // SQL-kyselyn tulokset
		int nro;
		try {
			conn = yhdista();
			if (conn != null) {
				conn.setAutoCommit(false);
				/*
				 * level - one of the following Connection constants:
				 * Connection.TRANSACTION_READ_UNCOMMITTED ,
				 * Connection.TRANSACTION_READ_COMMITTED ,
				 * Connection.TRANSACTION_REPEATABLE_READ or
				 * Connection.TRANSACTION_SERIALIZABLE.
				 */
				// eristyvyystason määritys
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				preparedStatement = conn.prepareStatement(sql);

				tulosjoukko = preparedStatement.executeQuery();
				if (tulosjoukko != null) {
					conn.commit();
					conn.close();
					while (tulosjoukko.next()) {
						nro = tulosjoukko.getInt("numero");
						if (lista == null)
							lista = new ArrayList<Integer>();

						lista.add(nro);
					}
					tulosjoukko.close();
				} else {
					lista = null;
					conn.commit();
					conn.close();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (conn != null && conn.isClosed() == false) {
				try {
					conn.rollback(); // peruuta transaktio
					conn.close(); // yhteys poikki
				} catch (Exception e) {
					e.printStackTrace();
					throw new SQLException();
				}
			}
		}
		return lista;
	}
	
}
