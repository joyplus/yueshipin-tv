package com.joyplus.charset;

public class EnCode {    
	public enum EnCoding{
		EncodingNone      (0),
		EncodingShiftJIS  ((1 << 0)),
		EncodingGBK       ((1 << 1)),
		EncodingBig5      ((1 << 2)),
		EncodingEUCKR     ((1 << 3)),
	    EncodingAll       ((EncodingShiftJIS.toInt() | EncodingGBK.toInt() 
	    		           | EncodingBig5.toInt() | EncodingEUCKR.toInt()));
		private int EnCode;		
		EnCoding(int encode){
			EnCode = encode;
		}
		public int toInt(){
			return EnCode;
		}
	};
	
	private static int possibleEncodings(String string){
		if(string ==  null || "".equals(string))
			return EnCoding.EncodingNone.toInt();
		return possibleEncodings(string.toCharArray());
	}
	private static int possibleEncodings(byte[] string){
		if(string == null || string.length<=0)
			return EnCoding.EncodingNone.toInt(); 
		return possibleEncodings(string.toString().toCharArray());
	}
	private static int possibleEncodings(char[] string){
		if(string == null || string.length<=0)
			return EnCoding.EncodingNone.toInt();
		int resoult  = EnCoding.EncodingAll.toInt();
		int  index = 0;
		byte value1,value2;
		while(index+4<string.length){
			value1 = (byte) string[index++];
			if((value1 & 0x80) != 0){
				value2 = (byte) string[index++];
				value1 = (byte) (((value1 << 6) & 0xC0) | (value2 & 0x3F));
				value2 = (byte) string[index++];
				if ((value2 & 0x80) != 0)
					value2 = (byte) (((value2 << 6) & 0xC0) | (string[index++] & 0x3F));
				int ch = (int)value1 << 8 | (int)value2;
				resoult &= AutoDetectEnCode.findPossibleEncodings(ch);
			}
		}
		return resoult;
	}
	public static String possibleEncode(byte[] string){
		return possibleEncode(string,"GBK");
	}
	public static String possibleEncode(char[] string){
		return possibleEncode(string,"GBK");
	}
	public static String possibleEncode(byte[] string,String defaultEnCode){
		return possibleEncode(string.toString().toCharArray(),defaultEnCode);
	}
	public static String possibleEncode(char[] string,String defaultEnCode){
		return possibleEncode(string,0,string.length,defaultEnCode);
	}
	public static String possibleEncode(byte[] string,int length,String defaultEnCode){
		return possibleEncode(string.toString().toCharArray(),0,string.length,defaultEnCode);
	}
	public static String possibleEncode(char[] string,int offset,int length,String defaultEnCode){
		if(string == null || string.length<=0)return defaultEnCode;
		String value = string.toString();
		if(offset<0)offset = 0;
		if(offset>=value.length())offset = value.length()-1;
		if(length<=0 || (length+offset>=value.length()))length = value.length() - offset;
		if(length<=0)length=1;
		int Resoult = possibleEncodings(value.substring(offset,(length+offset>=value.length())?value.length()-1:(length+offset)));
		if(Resoult == EnCoding.EncodingNone.toInt())return defaultEnCode;
		if((Resoult & EnCoding.EncodingGBK.toInt()) != 0){
			return "GBK";
		}else if((Resoult & EnCoding.EncodingBig5.toInt()) != 0){
			return "big5";
		}else {
			return "utf-8";
		}
	}
	public static boolean IsBig5EnCode(byte[] string){
		if(string == null || string.length<=0)return false;
		int  index = 0;
		byte value1,value2;
		while(index+4<string.length){
			value1 = (byte) string[index++];
			if((value1 & 0x80) != 0){
				value2 = (byte) string[index++];
				value1 = (byte) (((value1 << 6) & 0xC0) | (value2 & 0x3F));
				value2 = (byte) string[index++];
				if ((value2 & 0x80) != 0)
					value2 = (byte) (((value2 << 6) & 0xC0) | (string[index++] & 0x3F));
				int ch = (int)value1 << 8 | (int)value2;
				if(!AutoDetectEnCode.IsBig5Encodings(ch))return false;
			}
		}
		return true;
	}
}
