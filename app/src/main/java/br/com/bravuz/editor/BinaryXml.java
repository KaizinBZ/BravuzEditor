package br.com.bravuz.editor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Defensive, read-only viewer for Android's compiled XML. Resource IDs are not resolved. */
final class BinaryXml {
    private static final int MAX_STRINGS=100000, MAX_OUTPUT=1_000_000;
    private final ByteBuffer b;
    private List<String> strings;
    private final StringBuilder out=new StringBuilder();
    private int depth=0;
    private BinaryXml(byte[] data){b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);}
    static boolean looksBinary(byte[] d){return d.length>=8 && (d[0]&255)==3 && d[1]==0 && d[2]==8 && d[3]==0;}
    static String decode(byte[] data)throws IOException{return new BinaryXml(data).parse();}
    private int u16(int p)throws IOException{if(p<0||p+2>b.limit())throw bad();return b.getShort(p)&0xffff;}
    private int i32(int p)throws IOException{if(p<0||p+4>b.limit())throw bad();return b.getInt(p);}
    private IOException bad(){return new IOException("XML binário inválido ou não suportado.");}
    private String str(int i)throws IOException{if(i==-1)return "";if(strings==null||i<0||i>=strings.size())throw bad();return strings.get(i);}
    private int chunkSize(int at,int limit)throws IOException{int size=i32(at+4),head=u16(at+2);if(head<8||size<head||size>limit-at)throw bad();return size;}
    private void line(String text)throws IOException{if(out.length()+text.length()+depth*2+1>MAX_OUTPUT)throw new IOException("XML grande demais para visualização.");for(int i=0;i<depth;i++)out.append("  ");out.append(text).append('\n');}
    private String parse()throws IOException{
        if(!looksBinary(b.array()))throw bad();int total=chunkSize(0,b.limit());if(total!=b.limit())throw bad();int pos=u16(2);line("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        while(pos<total){int size=chunkSize(pos,total), type=u16(pos), header=u16(pos+2), end=pos+size;
            if(type==0x0001){if(strings!=null)throw bad();readStrings(pos,size,header);}
            else if(type==0x0102){if(header<16||size<36)throw bad();int name=i32(pos+20),attrOffset=u16(pos+24),attrSize=u16(pos+26),count=u16(pos+28);if(attrSize<20||count>10000)throw bad();long start=(long)pos+16+attrOffset;if(start<pos+36||(start+(long)attrSize*count)>end)throw bad();StringBuilder tag=new StringBuilder("<").append(str(name));
                for(int j=0;j<count;j++){int p=(int)start+j*attrSize;String ns=str(i32(p)), n=str(i32(p+4));int raw=i32(p+8),valueSize=u16(p+12),typeId=b.get(p+15)&255,value=i32(p+16);if(valueSize<8)throw bad();String val=raw!=-1?str(raw):typed(typeId,value);tag.append(' ').append(ns.equals("http://schemas.android.com/apk/res/android")?"android:":ns.isEmpty()?"":"{ "+ns+" }").append(n).append("=\"").append(escape(val)).append('"');if(tag.length()>MAX_OUTPUT)throw bad();}tag.append('>');line(tag.toString());depth++;if(depth>256)throw bad();}
            else if(type==0x0103){if(size<24||depth==0)throw bad();depth--;line("</"+str(i32(pos+20))+">");}
            else if(type==0x0104){if(size<28)throw bad();line(escape(str(i32(pos+16))));}
            pos=end;
        }if(pos!=total||depth!=0||strings==null)throw bad();return out.toString();
    }
    private String typed(int type,int value)throws IOException{switch(type){case 0:return "@null";case 1:return String.format(Locale.ROOT,"@0x%08x",value);case 2:return String.format(Locale.ROOT,"?0x%08x",value);case 3:return str(value);case 4:return Float.toString(Float.intBitsToFloat(value));case 16:return Integer.toString(value);case 17:return String.format(Locale.ROOT,"0x%08x",value);case 18:return value==0?"false":"true";case 28:case 29:case 30:case 31:return String.format(Locale.ROOT,"#%08x",value);default:return String.format(Locale.ROOT,"[tipo 0x%02x: 0x%08x]",type,value);}}
    private String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace("\"","&quot;");}
    private void readStrings(int base,int size,int header)throws IOException{
        if(header<28||size<28)throw bad();int count=i32(base+8),styles=i32(base+12),flags=i32(base+16),dataOffset=i32(base+20);if(count<0||count>MAX_STRINGS||styles<0||(long)header+4L*(count+styles)>size||dataOffset<header||dataOffset>=size)throw bad();int end=base+size;strings=new ArrayList<>(count);
        for(int i=0;i<count;i++){int rel=i32(base+header+i*4);if(rel<0||(long)base+dataOffset+rel>=end)throw bad();int[] ptr={base+dataOffset+rel};String value;if((flags&0x100)!=0){length8(ptr,end);int len=length8(ptr,end);if(len<0||ptr[0]+(long)len>=end||b.get(ptr[0]+len)!=0)throw bad();byte[] data=new byte[len];for(int j=0;j<len;j++)data[j]=b.get(ptr[0]+j);value=new String(data,StandardCharsets.UTF_8);}else{int len=length16(ptr,end);long bytes=(long)len*2;if(bytes>Integer.MAX_VALUE||ptr[0]+bytes+2>end||u16((int)(ptr[0]+bytes))!=0)throw bad();byte[] data=new byte[(int)bytes];for(int j=0;j<data.length;j++)data[j]=b.get(ptr[0]+j);value=new String(data,StandardCharsets.UTF_16LE);}strings.add(value);}
    }
    private int length8(int[] p,int end)throws IOException{if(p[0]>=end)throw bad();int x=b.get(p[0]++)&255;if((x&128)!=0){if(p[0]>=end)throw bad();x=((x&127)<<8)|(b.get(p[0]++)&255);}return x;}
    private int length16(int[] p,int end)throws IOException{if(p[0]+2>end)throw bad();int x=u16(p[0]);p[0]+=2;if((x&0x8000)!=0){if(p[0]+2>end)throw bad();x=((x&0x7fff)<<16)|u16(p[0]);p[0]+=2;}return x;}
}
