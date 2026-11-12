BravuzEditor Android integration notes
- Fork/integration adds BRAVUZ_ANDROID guards in sc1.c and libpawnc.c and adapts temporary files to TMPDIR in libpawnc.c.
- This is a modified source build of pawn-lang/compiler v3.10.10. Keep upstream notices; see ../LICENSE-pawn.txt.
- Native compiler ABI is invoked in-process using JNI. Do not run simultaneous compile calls on multiple threads: upstream compiler state is global and non-reentrant.
