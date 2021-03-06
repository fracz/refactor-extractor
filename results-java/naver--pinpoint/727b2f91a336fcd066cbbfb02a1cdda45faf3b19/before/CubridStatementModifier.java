package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

public class CubridStatementModifier extends AbstractModifier{
	public static byte[] modify(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classFileBuffer) {
		log("CubridStatementModifier modifing");
//		printClassInfo(javassistClassName);
		return changeMethod(classPool,classLoader,javassistClassName,classFileBuffer);
	}
	private static byte[] changeMethod(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateExecuteQueryMethod(classPool,cc);
			byte[] newClassfileBuffer = cc.toBytecode();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private static void updateExecuteQueryMethod(ClassPool classPool,CtClass cc) throws Exception {
		CtClass[] params=new CtClass[1];
		params[0]=classPool.getCtClass("java.lang.String");
		CtMethod serviceMethod=cc.getDeclaredMethod("executeQuery", params);
//		CtMethod serviceMethod=cc.getDeclaredMethod("executeQuery", null);
		log("*** Changing executeQuery method ");
//		serviceMethod.insertBefore(getExecuteQueryMethodBeforeInsertCode());
		serviceMethod.insertAfter(getExecuteQueryMethodAfterInsertCode());


	}
	@SuppressWarnings("unused")
	private static String getExecuteQueryMethodBeforeInsertCode() {
		StringBuilder sb=new StringBuilder();
//		sb.append("{");
//		sb.append("System.out.println(\"-----CUBRIDStatement.executeQuery() method is called\");");
//		sb.append("System.out.println(\"-----CUBRIDStatement.executeQuery(String) method is called\");");
//		sb.append("System.out.println(\"-----Query=[\"+com.profiler.util.QueryStringUtil.removeCarriageReturn($1)+\"]\");");
//		sb.append("}");
		return sb.toString();
	}
	private static String getExecuteQueryMethodAfterInsertCode() {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
//		sb.append("System.out.println(\"-----CUBRIDStatement.executeQuery(String) method is ended\");");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER+".putSqlQuery("+TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY+",$1);");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER+".put("+TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY+");");
		sb.append("}");
		return sb.toString();

	}
}