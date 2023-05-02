package org.luaj;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class MitsuhaLua {
    
    public static Globals newInstance(){
        Globals g = JsePlatform.standardGlobals();
        return g;
    }
    
    public static Object toJavaValue(LuaValue v,Class c){
        return CoerceLuaToJava.coerce(v,c);
    }
    
    public static LuaValue toLuaValue(Object o){
        return CoerceJavaToLua.coerce(o);
    }
    
}
