import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

import java.sql.SQLException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.naming.NamingException;



public class SF_EDO_SENDER_SaveGuidInDB extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();


		MbElement inRoot = inMessage.getRootElement();
		MbMessageAssembly outAssembly = null;
		try {
			
			MbElement inMsgId = inRoot.getFirstElementByPath("/MQMD/MsgId");
			Timestamp startTime = Timestamp.valueOf((String)inMessage.evaluateXPath("string(//*[local-name()='start'])"));
			String fileGuid = (String)inMessage.evaluateXPath("string(//*[local-name()='document_id'])");

			

			insertMessage(fileGuid);
			MbMessage outMessage = new MbMessage();
			MbElement outRoot = outMessage.getRootElement();
			MbElement mqmd = outRoot.createElementAsLastChild("MQHMD");
			mqmd.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "CodedCharSetId", 1208);
			mqmd.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Format", "MQHRF2");
			mqmd.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "MsgId", inMsgId.getValue());
			MbElement rfh2 = outRoot.createElementAsLastChild("MQHRF2");
			MbElement rf2Usr = rfh2.createElementAsLastChild(MbElement.TYPE_NAME, "usr", null);
			rf2Usr.createElementAsLastChild(MbElement.TYPE_NAME, "uid", fileGuid);
			outAssembly = new MbMessageAssembly(inAssembly ,outMessage);
			
		} catch (MbException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new MbUserException(this, "Ошибка доступа к Базе данных", "3006", "", e.toString(),
					null);
		}
		out.propagate(outAssembly);

	}

	private Timestamp formatDateTime(String dateTime) {
		Timestamp result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		if (dateTime != null) {
			String tmp = sdf.format(new Date(dateTime));
			result = Timestamp.valueOf(tmp);
		}
		return result;
	}
	
	private void insertMessage(String UID) throws MbException, SQLException, IOException, ClassNotFoundException, NamingException {
					DBConnectionInfo dbConnectionInfo = findConnectionDetails();
					Connection conn = getJDBCType4Connection(dbConnectionInfo.getJdbcProviderName(), JDBC_TransactionType.MB_TRANSACTION_AUTO);
					conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
					String execStrAdd = "INSERT INTO "+ dbConnectionInfo.getSchemaName() + "." + dbConnectionInfo.getTableName() + " " +
					"(UID, START, STOP, WSOPERATION,"+
					"EXTSYS, EXTUSER, INXML, OUTXML, WSRESULT) "+
					"VALUES (?,?,?,?,?,?,?,?,?)";
					PreparedStatement csAdd = conn.prepareStatement(execStrAdd);
					csAdd.setString(1, UID); 
					csAdd.execute();
					if (csAdd != null)
					csAdd.close();
					if (conn != null)
					conn.close();
}

	private DBConnectionInfo findConnectionDetails() throws MbException{
		String schemaName=(String)getUserDefinedAttribute("schemaName");
		String logTableName = (String)getUserDefinedAttribute("logTableName");
		String providerName = (String)getUserDefinedAttribute("JDBC_PROVIDER");
		String fieldName = null;		
		return new DBConnectionInfo(providerName, schemaName, logTableName, fieldName);
	}

	
	private class DBConnectionInfo{
		private String jdbcProviderName;
		private String schemaName;
		private String tableName;
		private String fieldName;
		
		
		public DBConnectionInfo(String jdbcProviderName, String schemaName,
				String tableName, String fieldName) {
			this.jdbcProviderName = jdbcProviderName;
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.fieldName = fieldName;
		}
		public String getJdbcProviderName() {
			return jdbcProviderName;
		}
		public void setJdbcProviderName(String jdbcProviderName) {
			this.jdbcProviderName = jdbcProviderName;
		}
		public String getSchemaName() {
			return schemaName;
		}
		public void setSchemaName(String schemaName) {
			this.schemaName = schemaName;
		}
		public String getTableName() {
			return tableName;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		
		
	}


}
