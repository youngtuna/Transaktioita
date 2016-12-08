package controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.Dao;

/**
 * Servlet implementation class NiteenLainausOhjelma
 */
@WebServlet("/NiteenLainausOhjelma")
public class NiteenLainausOhjelma extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		RequestDispatcher disp;
		String action = request.getParameter("action");
		
		if (action.equals("vahvista lainaus"))
		{
			vahvistaLainaus(request, response);
		}
		
	}
	
	private void vahvistaLainaus(HttpServletRequest request, HttpServletResponse response) {
		Dao dao = new Dao();
		RequestDispatcher disp;
		String asiakasnumero = request.getParameter("asiakas");
		String nide = request.getParameter("nide");
		System.out.println(asiakasnumero);
		System.out.println(nide);
		String[] splitted;
		int nro;
		String isbn;
		int nidenro;
		
		try {
			
			nro = Integer.parseInt(asiakasnumero.trim());
			splitted = nide.split("\\s+");
			isbn = splitted[0];
			nidenro = Integer.parseInt(splitted[1]);
			System.out.println("ISBN: " +isbn);
			System.out.println("NIDENRO: " +nidenro);
			System.out.println("ASIAKASNRO: " +nro);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
