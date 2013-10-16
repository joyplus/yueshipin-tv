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
	
	public static int possibleEncodings(String string){
		if(string ==  null || "".equals(string))
			return EnCoding.EncodingNone.toInt();
		return possibleEncodings(string.toCharArray());
	}
	public static int possibleEncodings(byte[] string){
		if(string == null || string.length<=0)
			return EnCoding.EncodingNone.toInt(); 
		return possibleEncodings(string.toString().toCharArray());
	}
	public static int possibleEncodings(char[] string){
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
	
}
