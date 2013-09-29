package com.joyplus.sub;

import java.io.InputStream;

public interface JoyplusSubInterface {
	   /*Interface of sub type*/      
       public enum SubContentType{
    	   SUB_UNKNOW (0),
    	   SUB_SRT    (1),
    	   SUB_ASS    (2),
    	   SUB_SSA    (3),
    	   SUB_STL    (4),
    	   SUB_SCC    (5),
    	   SUB_MAX    (5);
    	   private int Type;
    	   SubContentType(int type){
    		   Type = type;
    	   }
    	   public int toInt(){
    		   return Type;
    	   }
    	   public String toExtension(){
    		   switch(Type){
    		   case 1: return ".srt";
    		   case 2: return ".ass";
    		   case 3: return ".ssa";
    		   case 4: return ".stl";
    		   case 5: return ".scc";
    		   }
    		   return "";
    	   }
    	   public SubContentType toType(String extension){
    		   if(extension.equalsIgnoreCase("srt"))return SubContentType.SUB_SRT;
    		   else if(extension.equalsIgnoreCase("ass"))return SubContentType.SUB_ASS;
    		   else if(extension.equalsIgnoreCase("ssa"))return SubContentType.SUB_SSA;
    		   else if(extension.equalsIgnoreCase("stl"))return SubContentType.SUB_STL;
    		   else if(extension.equalsIgnoreCase("scc"))return SubContentType.SUB_SCC;
    		   else return SubContentType.SUB_UNKNOW;
    	   } 
       }
        
       public void parse(byte[] sub);
       
       public void parseLocal();
       
}
