package hmi.flipper2.javascript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import hmi.flipper2.FlipperException;

public class JsEngine {

	private ScriptEngineManager mgr;
	private ScriptEngine engine;
	
	public JsEngine() throws FlipperException {
		js_init();
	}
	
	protected void js_init() throws FlipperException {
		this.mgr = new ScriptEngineManager();
		this.engine = mgr.getEngineByName("nashorn");
	}

	public JsValue execute(String script) throws FlipperException {
		try {
			JsValue res = null;
			
			// System.out.println("!!JSE: "+script);
			Object retval = engine.eval(script);
			// JSOBject jso;
			if ( retval != null ) {
				res = new JsObjectValue(retval);
				// System.out.println("!!!RETURN["+retval.getClass().getName()+"]: "+res);
			}
			return res;
		} catch (ScriptException e) {
			throw new FlipperException(e, script);
		}
	}
	
	public Object eval(String script) throws FlipperException {
		try {
			return engine.eval(script);
		} catch (ScriptException e) {
			throw new FlipperException(e, script);
		}
	}
	
	public boolean condition(String script) throws FlipperException {
		try {
			Object retval = engine.eval(script);
			if (retval != null) {
				try {
					return ((Boolean) retval).booleanValue();
				} catch (ClassCastException e) {
				}				
			}
			throw new FlipperException("Condition not Boolean: " + script);
		} catch (ScriptException e) {
			throw new FlipperException(e, script);
		}
	}
	
	public String getJSONfromJs(String js_expr) throws FlipperException {
		try {
			return (String)engine.eval("JSON.stringify("+js_expr+")");
		} catch (ScriptException e) {
			throw new FlipperException(e, "JSON.stringify("+js_expr+")");
		}
	}
	
	public void assignJSONtoJs(String var, String json_expr) throws FlipperException {
		String script = null;
		
		try {
			script = var + "=" + "JSON.parse("+escapeForJava(json_expr, true)+")";
			engine.eval(script);
		} catch (ScriptException e) {
			throw new FlipperException(e, script);
		}
	}
	
	public void assignObject2Js(String var, Object java_object) throws FlipperException {		
		engine.put("xfervalue", java_object);
		eval( var + "=" +"xfervalue");
	}
	
	public static String escapeForJava( String value, boolean quote )
	{
	    StringBuilder builder = new StringBuilder();
	    if( quote )
	        builder.append( "\"" );
	    for( char c : value.toCharArray() )
	    {
	        if( c == '\'' )
	            builder.append( "\\'" );
	        else if ( c == '\"' )
	            builder.append( "\\\"" );
	        else if( c == '\r' )
	            builder.append( "\\r" );
	        else if( c == '\n' )
	            builder.append( "\\n" );
	        else if( c == '\t' )
	            builder.append( "\\t" );
	        else if( c < 32 || c >= 127 )
	            builder.append( String.format( "\\u%04x", (int)c ) );
	        else
	            builder.append( c );
	    }
	    if( quote )
	        builder.append( "\"" );
	    return builder.toString();
	}

	
	
	/*
	 * 
	 * 
	 */
	
//	public static void main(String[] args) {
//		test();
//	}
//
//	public static String is = "var is = { \"name\":\"John\", \"age\":30, \"car\":null }";
//	
//	public static void test() {
//		try {
//			ScriptEngineManager mgr = new ScriptEngineManager();
//			ScriptEngine engine = mgr.getEngineByName("JavaScript");
//			// engine.put("a", 41);
//			engine.eval(is);
//			System.out.println(engine.eval("is;").getClass().getName());
//			System.out.println("STR=" + engine.eval("JSON.stringify(is);"));
//			// JSON.parse()
//			// Eerste versie werkt met JSON.stringify en JSON.parse als setting
//			//
//			System.out.println(engine.eval("is[\"name\"];"));
//			engine.eval("var a=43");
//			engine.eval("var cars = [\"Saab\", \"Volvo\", \"BMW\"]");
//			engine.eval("function myFunction() { return cars[2]; }");
//			System.out.println(engine.eval("cars[1]"));
//			System.out.println(engine.eval("myFunction()"));
//		} catch (ScriptException e) {
//			System.err.println("!Caught: " + e);
//		}

//	}
}