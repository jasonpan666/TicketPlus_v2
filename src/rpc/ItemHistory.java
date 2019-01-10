package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		// optional
		String userId = session.getAttribute("user_id").toString(); 
		
		// String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();
		
		DBConnection conn = DBConnectionFactory.getConnection();
		try {
			Set<Item> items = conn.getFavoriteItems(userId);
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				array.put(obj);
			}
			
			RpcHelper.writeJsonArray(response, array);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 // allow access only if session exists
		 HttpSession session = request.getSession(false);
		 if (session == null) {
			 response.setStatus(403);
			 return;
		 }
		
		 DBConnection connection = DBConnectionFactory.getConnection();
	   	 try {
	   		 JSONObject input = RpcHelper.readJSONObject(request);
	   	     // optional
	 		 String userId = session.getAttribute("user_id").toString(); 
	   		 // String userId = input.getString("user_id");
	   		 JSONArray array = input.getJSONArray("favorite");
	   		 List<String> itemIds = new ArrayList<>();
	   		 for(int i = 0; i < array.length(); ++i) {
	   			 itemIds.add(array.getString(i));
	   		 }
	   		 connection.setFavoriteItems(userId, itemIds);
	   		 RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
	   		 
	   	 } catch (Exception e) {
	   		 e.printStackTrace();
	   	 } finally {
	   		 connection.close();
	   	 }
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 // allow access only if session exists
		 HttpSession session = request.getSession(false);
		 if (session == null) {
			 response.setStatus(403);
			 return;
		 }
		 
	   	 DBConnection connection = DBConnectionFactory.getConnection();
	   	 try {
	   		 JSONObject input = RpcHelper.readJSONObject(request);
	   	     // optional
	 		 String userId = session.getAttribute("user_id").toString();
	   		 // String userId = input.getString("user_id");
	   		 JSONArray array = input.getJSONArray("favorite");
	   		 List<String> itemIds = new ArrayList<>();
	   		 for(int i = 0; i < array.length(); ++i) {
	   			 itemIds.add(array.getString(i));
	   		 }
	   		 connection.unsetFavoriteItems(userId, itemIds);
	   		 RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));   		 
	   	 } catch (Exception e) {
	   		 e.printStackTrace();
	   	 } finally {
	   		 connection.close();
	   	 }
	}

}
