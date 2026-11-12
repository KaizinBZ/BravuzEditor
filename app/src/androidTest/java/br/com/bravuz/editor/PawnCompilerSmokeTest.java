package br.com.bravuz.editor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PawnCompilerSmokeTest {
    @Test public void compilesPawnToAmxOnDevice() throws Exception {
        File dir=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(),"pawn-smoke");
        dir.mkdirs();File source=new File(dir,"smoke.pwn");File output=new File(dir,"smoke.amx");
        try(FileOutputStream out=new FileOutputStream(source)){out.write("public OnGameModeInit()\\n{\\n    return 1;\\n}\\n".getBytes("UTF-8"));}
        PawnCompiler.compile(InstrumentationRegistry.getInstrumentation().getTargetContext(),source,output,dir);
        assertTrue("AMX output exists",output.isFile());assertTrue("AMX output is non-empty",output.length()>32);
        try(RandomAccessFile raf=new RandomAccessFile(output,"r")){raf.seek(4);int lo=raf.readUnsignedByte(),hi=raf.readUnsignedByte();assertEquals("AMX magic",0xe0,lo);assertEquals("AMX magic",0xf1,hi);}
    }
}
