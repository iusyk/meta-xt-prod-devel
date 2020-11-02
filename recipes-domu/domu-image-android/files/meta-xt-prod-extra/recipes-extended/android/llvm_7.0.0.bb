
VERSION="7.0.0"
VERSION_FOLDERS="7.0.0"
FOLDER_POSTFIX="70"
LIBRARY_POSTFIX="-img"

SRC_URI="http://llvm.org/releases/7.0.0/llvm-${VERSION}.src.tar.xz;name=llvm;destsuffix=ndk-bundle/apps/llvm${FOLDER_POSTFIX}"
SRC_URI[llvm.md5sum] = "e0140354db83cdeb8668531b431398f0"
SRC_URI[llvm.sha256sum] = "8bc1f844e6cbde1b652c19c1edebc1864456fd9c78b8c1bea038e51b363fe222"

LLVM_SOURCE_DIR="${WORKDIR}/ndk-bundle/apps/llvm${FOLDER_POSTFIX}"

CLANG_ARCHIVES="clangFrontendTool clangFrontend clangDriver clangSerialization clangCodeGen clangParse clangSema clangRewriteFrontend clangRewrite clangAnalysis clangEdit clangAST clangASTMatchers clangLex clangBasic"

LLVM_ARCHIVES="LLVMLTO LLVMPasses LLVMSymbolize LLVMDebugInfoDWARF LLVMTestingSupport LLVMLibDriver LLVMAsmPrinter LLVMCoverage LLVMTableGen LLVMOption LLVMipo LLVMVectoriz
e LLVMLinker LLVMIRReader LLVMMIRParser LLVMAsmParser LLVMCodeGen LLVMScalarOpts LLVMInstCombine LLVMTransformUtils LLVMBitWriter LLVMTarget LLVMAnalysis LLVMProfileData LL
VMObject LLVMBitReader LLVMMC LLVMCore LLVMBinaryFormat LLVMSupport LLVMDemangle"

do_configure() {
#    if [ -d "${WORKDIR}/llvm-${VERSION}.src" ]; then
#       mv ${WORKDIR}/llvm-${VERSION}.src ${WORKDIR}/ndk-bundle/apps/llvm${FOLDER_POSTFIX}
#    fi
#    if [ -d "${WORKDIR}/llvm-${VERSION}.src" ]; then
#       mv ${WORKDIR}/llvm-${VERSION}.src ${WORKDIR}/ndk-bundle/apps/llvm${FOLDER_POSTFIX}
#    fi
}

do_compile() {
    common_llvm_flags="-DCMAKE_BUILD_TYPE=Release -DLLVM_EXTERNAL_CLANG_SOURCE_DIR=${CLANG_SOURCE_DIR} -DLLVM_BUILD_RUNTIME=Off -DLLVM_INCLUDE_TESTS=Off -DLLVM_ENABLE_BACKT
RACES=Off -DLLVM_OPTIMIZED_TABLEGEN=On -DLLVM_ENABLE_ZLIB=Off -DLLVM_TARGETS_TO_BUILD= -DLLVM_ENABLE_TERMINFO=Off -DLLVM_BUILD_TOOLS=Off -DLLVM_ENABLE_LIBEDIT=Off -DLLVM_BU
ILD_UTILS=Off -DLLVM_BUILD_RUNTIMES=Off -DCLANG_ENABLE_OBJC_REWRITER=Off -DCLANG_ENABLE_ARCMT=Off -DCLANG_PLUGIN_SUPPORT=Off -DCLANG_ENABLE_STATIC_ANALYZER=Off -DLLVM_PARAL
LEL_LINK_JOBS=1 -DLLVM_TABLEGEN=${LLVM_SOURCE_DIR}/host_build/bin/llvm-tblgen${LIBRARY_POSTFIX} -DCLANG_TABLEGEN=${LLVM_SOURCE_DIR}/host_build/bin/clang-tblgen${LIBRARY_POS
TFIX} ${CONFIGURE_CCACHE}"
    
    target_clang_libs=`echo "${CLANG_ARCHIVES}" | sed "s/\>/${LIBRARY_POSTFIX}.a/g" | sed "s/clang/lib\/libclang/g"`
    target_llvm_libs=`echo "${LLVM_ARCHIVES}" | sed "s/\>/${LIBRARY_POSTFIX}.a/g" | sed "s/LLVM/lib\/libLLVM/g"`

    cd ${LLVM_SOURCE_DIR}
    rm -rf host_build
    mkdir host_build
    cd host_build

    CC=clang
    CXX=clang++

    if [ ! -f ${LLVM_SOURCE_DIR}/host_build/.signature ]; then

        cmake ${common_llvm_flags} -DLLVM_ENABLE_ASSERTIONS=On ${LLVM_SOURCE_DIR}

        cd ${WORKDIR}/repo

        bash -c "source build/envsetup.sh && \
             lunch xenvm-userdebug && \
             make -j16 -C ${LLVM_SOURCE_DIR}/host_build clang-tblgen llvm-tblgen llvm-as llvm-link && \
             make -j16 -C ${LLVM_SOURCE_DIR}/host_build clang ${CLANG_ARCHIVES} ${LLVM_ARCHIVES} \
        "
        cd ${LLVM_SOURCE_DIR}/host_build

        # Move the created binaries into ${NDK_ROOT}/out/local/host/bin/

        bins_folder=${WORKDIR}/ndk-bundle/out/local/host/bin/

        mkdir -p ${bins_folder}
        mv bin/clang${LIBRARY_POSTFIX} bin/llvm-as${LIBRARY_POSTFIX} bin/llvm-link${LIBRARY_POSTFIX} ${bins_folder}

        # Move the created libraries into ${NDK_ROOT}/out/local/host/lib/

        libs_folder=${WORKDIR}/ndk-bundle/out/local/host/lib/llvm${LIBRARY_POSTFIX}${FOLDER_POSTFIX}/

        mkdir -p ${libs_folder}
        mv ${target_llvm_libs} ${target_clang_libs} ${libs_folder}

        build_signature=$(md5sum ${LLVM_SOURCE_DIR}/host_build/Makefile)
        echo ${build_signature} > ${LLVM_SOURCE_DIR}/host_build/.signature

    else
      echo "Signature exists ${LLVM_SOURCE_DIR}/host_build/.signature, no needs to build again."
    fi
}
