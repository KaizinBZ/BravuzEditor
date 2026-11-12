Embedded Pawn Compiler v3.10.10
Upstream: https://github.com/pawn-lang/compiler (v3.10.10).
The Android/JNI glue and build files adapt the upstream compiler; this is not the original Windows SA-MP compiler executable. Upstream license and attribution are retained in LICENSE-pawn.txt and source files.

The app compiles locally and does not upload source code. It uses -Z compatibility mode. It includes generic Pawn `.inc` files; SA-MP/open.mp declarations such as a_samp.inc and plugin-specific includes must be provided by the user, ideally alongside the selected `.pwn` inside a ZIP project. Code pages are disabled in this build; source encoding should be UTF-8 / compatible ASCII.
