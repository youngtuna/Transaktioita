package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bean.Asiakas;
import bean.Lainaus;
import bean.Nide;
import dao.Dao;

/**
 * Servlet implementation class LainaustenSelausOhjelma
 */
@WebServlet("/LainaustenSelausOhjelma")
public class LainaustenSelausOhjelma extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		RequestDispatcher disp;
		String action = request.getParameter("action");
		
		if (action == null || (action!= null && action.equalsIgnoreCase("etusivu")))
		{
			haeLainausNumerot(request, response);
		}
		else
		if (action.equals("Hae lainaus"))
		{
			haeLainaus(request, response);
		}
		else
		if (action.equals("Hae kaikki lainaukset"))
		{
			haeLainaukset(request, response);
		}
		else
		if (action.equals("Uusi lainaus"))
		{
			uusiLainaus(request, response);
		}

	}
	
	private void haeLainausNumerot(HttpServletRequest request, HttpServletResponse response) 
	{
		Dao dao = new Dao();
		List<Integer> lista = null;
		RequestDispatcher disp;
		try
		{
			lista = dao.haeLainausNumerot();
			if (lista != null)
			{
				for (int i = 0; i<lista.size();i++)
				   System.out.println(lista.get(i) );
				   
				request.setAttribute("lainausnumerot", lista);
				disp = request.getRequestDispatcher("etusivu.jsp");
				disp.forward(request, response);
			}
		}
		catch (SQLException e)
		{
			System.out.println("jossain virhe");
		}
		catch (Exception e)
		{
			System.out.println("ajurissa virhe");
		}
	}
	
	private void haeLainaus(HttpServletRequest request, HttpServletResponse response) 
	{
		RequestDispatcher disp;
		Dao dao = new Dao();
		Lainaus lainaus;
		String lainausnumero = request.getParameter("lainausnumero");
		int nro;
		
		System.out.println(lainausnumero);
		try
		{
			nro = Integer.parseInt(lainausnumero.trim());
			System.out.println("haetaan lainauksella " + nro);
			lainaus = dao.haeLainaus(nro);
			
			if (lainaus != null)
			{
				request.setAttribute("lainaus", lainaus);
				disp = request.getRequestDispatcher("YksiLainaus.jsp");
				disp.forward(request, response);
			}
			else
			{
				request.setAttribute("eiLoydy", true);
				disp = request.getRequestDispatcher("etusivu.jsp");
				disp.forward(request, response);
			}

		}
		catch (SQLException e)
		{
			System.out.println("jossain virhe");
		}
		catch (Exception e)
		{
			System.out.println(" ajurissa virhe");
		}
	}
	
	private void haeLainaukset(HttpServletRequest request, HttpServletResponse response) 
	{
		RequestDispatcher disp;
		Dao dao = new Dao();
		ArrayList<Lainaus> lista=null;
		
		try
		{
			lista = dao.haeLainaukset();
			
			if (lista != null)
			{
			
				for (int i = 0 ;i<lista.size();i++)
				{
					System.out.println();
					System.out.println(lista.get(i));
				}
				request.setAttribute("lainauslista", lista);
				disp = request.getRequestDispatcher("KaikkiLainaukset.jsp");
				disp.forward(request, response);
			}
			
		}
		catch (SQLException e)
		{
			System.out.println("jossain virhe");
		}
		catch (Exception e)
		{
			System.out.println(" ajurissa virhe");
		}
	}
	
	private void uusiLainaus(HttpServletRequest request, HttpServletResponse response) 
	{
		RequestDispatcher disp;
		Dao dao = new Dao();
		ArrayList<Nide> niteet=null;
		ArrayList<Asiakas> asiakkaat=null;
		
		// Haetaan lainattavat niteet ja kaikki asiakkaat
		try
		{
			niteet = dao.haeNiteet();
			asiakkaat = dao.haeAsiakkaat();
			
			if (niteet != null && asiakkaat != null )
			{
				request.setAttribute("asiakkaat", asiakkaat);
				request.setAttribute("niteet", niteet);
				disp = request.getRequestDispatcher("lisaaLainaus.jsp");
				disp.forward(request, response);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("jossain virhe");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("ajurissa virhe");
		}
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}