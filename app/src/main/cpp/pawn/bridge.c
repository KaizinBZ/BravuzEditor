#include <jni.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <libgen.h>
#include <stdlib.h>
#include "sc.h"
static char output[131072];
static size_t used;
static void append(const char *s){if(!s)return;size_t n=strlen(s);if(n>sizeof(output)-used-1)n=sizeof(output)-used-1;if(n){memcpy(output+used,s,n);used+=n;output[used]=0;}}
int pc_printf(const char *format,...){char b[4096];va_list ap;va_start(ap,format);int r=vsnprintf(b,sizeof(b),format,ap);va_end(ap);append(b);return r;}
int pc_error(int number,char *message,char *filename,int first,int last,va_list ap){char b[4096];int n=snprintf(b,sizeof(b),"%s(%d): %s %03d: ",filename?filename:"source",last,number>=200?"warning":"error",number);if(n<0)n=0;if((size_t)n<sizeof(b))vsnprintf(b+n,sizeof(b)-(size_t)n,message,ap);append(b);append("\n");return 0;}
static char *fromj(JNIEnv *e,jstring s){return (char*)(*e)->GetStringUTFChars(e,s,0);}
JNIEXPORT jstring JNICALL Java_br_com_bravuz_editor_PawnCompiler_compileNative(JNIEnv *e,jclass c,jstring source,jstring outputPath,jstring includes,jstring error){
 (void)c;used=0;output[0]=0;
 char *src=fromj(e,source),*out=fromj(e,outputPath),*inc=fromj(e,includes),*err=fromj(e,error);
 char *argv[80];char incbuf[64][4096];int argc=0;argv[argc++]="pawncc";argv[argc++]=src;
 char *includeCopy=strdup(inc);char *save=NULL;int slot=0;for(char *part=includeCopy?strtok_r(includeCopy,"|",&save):NULL;part&&slot<64;part=strtok_r(NULL,"|",&save),slot++){snprintf(incbuf[slot],sizeof(incbuf[slot]),"-i%s",part);argv[argc++]=incbuf[slot];}free(includeCopy);
 char outArg[4096],errArg[4096];snprintf(outArg,sizeof(outArg),"-o%s",out);snprintf(errArg,sizeof(errArg),"-e%s",err);argv[argc++]=outArg;argv[argc++]=errArg;argv[argc++]="-Z";argv[argc++]="-O2";
 char *sourceDir=strdup(src);if(sourceDir){setenv("TMPDIR",dirname(sourceDir),1);free(sourceDir);}
 int code=pc_compile(argc,argv);if(code!=0){char b[128];snprintf(b,sizeof(b),"Compilação falhou (código %d).\n",code);append(b);}(*e)->ReleaseStringUTFChars(e,source,src);(*e)->ReleaseStringUTFChars(e,outputPath,out);(*e)->ReleaseStringUTFChars(e,includes,inc);(*e)->ReleaseStringUTFChars(e,error,err);return (*e)->NewStringUTF(e,output);
}
