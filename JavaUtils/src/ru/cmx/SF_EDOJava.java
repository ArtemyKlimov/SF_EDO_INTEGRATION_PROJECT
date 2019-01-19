package ru.cmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.ibm.broker.plugin.MbElement;
import javax.naming.directory.SearchControls;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.digest.DigestUtils;


public class SF_EDOJava {
	public static String calculateFileHash(byte[] document) {
	    InputStream in = null;
	    String hex = null;
	    try {
	        in = new ByteArrayInputStream(document);
	        hex = DigestUtils.md5Hex(in);
	        StringBuilder str = new StringBuilder(hex);
	        rearrangeSymbols(str, 0, 3);
	        rearrangeSymbols(str, 1, 5);
	        rearrangeSymbols(str, 8, 26);
	        hex = str.toString();
	        return hex;
	    } catch(IOException ioe) {
	    	ioe.printStackTrace();
	    } finally {
	        if (in != null) {
	            try {
	                in.close();
	            } catch (IOException e) {
	            	e.printStackTrace();              
	            }
	        }
	    }
	    return hex;
	}
	
	private static void rearrangeSymbols(StringBuilder str, int ind1, int ind2) {
	    char tmp = str.charAt(ind2);
	    str.setCharAt(ind2, str.charAt(ind1));
	    str.setCharAt(ind1, tmp);
	}
	
	
	public static String getUSIDs(String pin, 
			String userName, 
			String password, 
			String server, 
			String port, 
			String searchBase, 
			String oridAttr, 
			String usidAttr, 
			MbElement[] element) {

		String filter = "(" + oridAttr + "=" + pin + ")";
		String ldapServer = "ldap://" + server + ":" + port;
		String[] attrIDs = {usidAttr};
		SearchControls ctls = new SearchControls();
		ctls.setReturningAttributes(attrIDs);
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapServer);
		if(userName != null) {
			env.put(Context.SECURITY_PRINCIPAL, userName);
		}
		if(password != null) {
			env.put(Context.SECURITY_CREDENTIALS, password);
		}
		DirContext ctx;
		int result = 0;
		try {
			MbElement usids = element[0];
			ctx = new InitialDirContext(env);
			NamingEnumeration e = ctx.search(searchBase, filter, ctls);
			while (e.hasMore()) {
				result++;
			    SearchResult entry = (SearchResult) e.next();
			    String usid = entry.getAttributes().get(usidAttr).get().toString();
			    usids.createElementAsLastChild(MbElement.TYPE_NAME, "usid", usid); //у пользователя может быть несколько профайлов       
			}
		} catch (Exception e1) {
			return e1.getMessage();
		
		}
		if (result == 0)
			return "No results found";
		return null;
		}

}
