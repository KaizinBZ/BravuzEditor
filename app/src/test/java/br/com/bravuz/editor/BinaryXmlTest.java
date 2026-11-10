package br.com.bravuz.editor;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryXmlTest {
    private static byte[] example(){
        ByteBuffer b=ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);
        b.putShort((short)3).putShort((short)8).putInt(0);
        int pool=b.position();b.putShort((short)1).putShort((short)28).putInt(0).putInt(1).putInt(0).putInt(0x100).putInt(32).putInt(0).putInt(0);
        b.put((byte)4).put((byte)4).put(new byte[]{'r','o','o','t',0});while((b.position()-pool)%4!=0)b.put((byte)0);b.putInt(pool+4,b.position()-pool);
        b.putShort((short)0x102).putShort((short)16).putInt(36).putInt(1).putInt(-1).putInt(-1).putInt(0).putShort((short)20).putShort((short)20).putShort((short)0).putShort((short)0).putShort((short)0).putShort((short)0);
        b.putShort((short)0x103).putShort((short)16).putInt(24).putInt(2).putInt(-1).putInt(-1).putInt(0);
        b.putInt(4,b.position());byte[] d=new byte[b.position()];b.rewind();b.get(d);return d;
    }
    @Test public void binaryXmlDecodes() throws Exception {byte[] d=example();assertTrue(BinaryXml.looksBinary(d));String result=BinaryXml.decode(d);assertTrue(result.contains("<root>"));assertTrue(result.contains("</root>"));}
    @Test public void invalidDataIsRejected() throws Exception {byte[] d=example();d[12]=(byte)255;try {BinaryXml.decode(d);fail("expected IOException");}catch(IOException expected){}}
    @Test public void ordinaryTextIsNotBinary(){assertFalse(BinaryXml.looksBinary("<x/>".getBytes()));}
}
