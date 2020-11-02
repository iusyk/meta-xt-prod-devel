
VERSION="7.0.0"
VERSION_FOLDERS="7.0.0"
FOLDER_POSTFIX="70"
LIBRARY_POSTFIX="-img"

SRC_URI="http://llvm.org/releases/7.0.0/cfe-${VERSION}.src.tar.xz;name=cfe;destsuffix=ndk-bundle/apps/clang${FOLDER_POSTFIX}"

SRC_URI[cfe.md5sum] = "2ac5d8d78be681e31611c5e546e11174"
SRC_URI[cfe.sha256sum] = "550212711c752697d2f82c648714a7221b1207fd9441543ff4aa9e3be45bba55"

do_configure() {
   
}
