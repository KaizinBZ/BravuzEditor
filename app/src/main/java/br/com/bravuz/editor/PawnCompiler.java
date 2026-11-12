package br.com.bravuz.editor;
import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Embedded Pawn community compiler; compiles locally through JNI. */
final class PawnCompiler {
    static { System.loadLibrary("bravuzpawn"); }
    private PawnCompiler(){}
    private static native String compileNative(String source,String output,String includePaths,String errorPath);
    static synchronized File compile(Context context,File source,File output,File project)throws IOException {
        File root=new File(context.getFilesDir(),"pawn-compiler");File includes=new File(root,"include");if(!includes.isDirectory()&&!includes.mkdirs())throw new IOException("Não consegui preparar includes Pawn.");copyAssets(context,"pawn/include",includes);
        ArrayList<String> dirs=new ArrayList<>();dirs.add(includes.getAbsolutePath());if(source.getParentFile()!=null)dirs.add(source.getParentFile().getAbsolutePath());if(project!=null)collect(project,dirs,64);StringBuilder paths=new StringBuilder();for(String d:dirs){if(paths.length()>0)paths.append('|');paths.append(d.replace("|",""));}
        File errors=new File(output.getParentFile(),"pawn-errors.txt");String log=compileNative(source.getAbsolutePath(),output.getAbsolutePath(),paths.toString(),errors.getAbsolutePath());
        if(!output.isFile()||output.length()==0)throw new IOException(log.isEmpty()?"Não foi gerado .amx. Verifique erros e includes da gamemode.":log);return output;
    }
    private static void collect(File dir,ArrayList<String> out,int limit){if(out.size()>=limit)return;File[] fs=dir.listFiles();if(fs==null)return;for(File f:fs)if(f.isDirectory()){out.add(f.getAbsolutePath());collect(f,out,limit);if(out.size()>=limit)return;}}
    private static void copyAssets(Context c,String path,File dest)throws IOException {String[] names=c.getAssets().list(path);if(names==null)throw new IOException("Não encontrei os includes do Pawn.");for(String name:names){String child=path+"/"+name;File target=new File(dest,name);String[] nested=c.getAssets().list(child);if(nested!=null&&nested.length>0){if(!target.isDirectory()&&!target.mkdirs())throw new IOException("Falha criando pasta include.");copyAssets(c,child,target);}else try(java.io.InputStream in=c.getAssets().open(child);java.io.FileOutputStream out=new java.io.FileOutputStream(target)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}}}
}
