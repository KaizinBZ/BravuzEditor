cmake_minimum_required(VERSION 3.22.1)
project(bravuzpawn C)
set(PAWN ${CMAKE_CURRENT_SOURCE_DIR})
set(VERSION_MAJOR 3)
set(VERSION_MINOR 10)
set(VERSION_BUILD 10)
set(VERSION 3.10.10)
set(VERSION_STR 3.10.10)
set(VERSION_INT 778)
set(COMP ${PAWN}/compiler)
set(AMX ${PAWN}/amx)
set(LINUX ${PAWN}/linux)
configure_file(${COMP}/version.h.in ${CMAKE_CURRENT_BINARY_DIR}/version.h @ONLY)
add_library(bravuzpawn SHARED
 ${COMP}/hashtable/wrap_hashtable.c
 ${COMP}/libpawnc.c ${COMP}/lstring.c ${COMP}/memfile.c
 ${CMAKE_CURRENT_SOURCE_DIR}/bridge.c
 ${COMP}/sc1.c ${COMP}/sc2.c ${COMP}/sc3.c ${COMP}/sc4.c ${COMP}/sc5.c ${COMP}/sc6.c ${COMP}/sc7.c
 ${COMP}/sci18n.c ${COMP}/sclist.c ${COMP}/scmemfil.c ${COMP}/scstate.c ${COMP}/scvars.c
 ${LINUX/binreloc.c}
)
target_include_directories(bravuzpawn PRIVATE ${COMP} ${COMP}/hashtable ${AMX} ${LINUX} ${CMAKE_CURRENT_BINARY_DIR})
target_compile_definitions(bravuzpawn PRIVATE LINUX BRAVUZ_ANDROID NO_MAIN NO_CODEPAGE)
target_compile_options(bravuzpawn PRIVATE -fvisibility=hidden -Wno-implicit-function-declaration)
target_link_libraries(bravuzpawn log)
