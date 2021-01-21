require inc/xt_shared_env.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"


GUEST_DOMA = "${@bb.utils.contains('XT_GUESTS_INSTALL', 'doma', 'doma', '', d)}"
GUEST_DOMU = "${@bb.utils.contains('XT_GUESTS_INSTALL', 'domu', 'domu', '', d)}"

UBOOT_CONFIG[doma] = "xenguest_arm64_android_defconfig"
UBOOT_CONFIG_prepend = "${GUEST_DOMA} "
UBOOT_CONFIG[domu] = "xenguest_arm64_defconfig"
UBOOT_CONFIG_prepend = "${GUEST_DOMU} "
UBOOT_CONFIG[domd] = "${XT_DOMD_DEFCONFIG}"
UBOOT_CONFIG_prepend = "domd "


----

GUEST_DOMA = "${@bb.utils.contains('XT_GUESTS_INSTALL', 'doma', 'doma', '', d)}"

UBOOT_CONFIG[doma] = "xenguest_arm64_android_defconfig"
UBOOT_CONFIG_prepend = "${GUEST_DOMA} "

SRCREV = "${AUTOREV}"
SRC_URI = "\
    git://github.com/xen-troops/u-boot.git;protocol=https;branch=android-master; \
"

FILES_${PN} += " \
    ${@bb.utils.contains('XT_GUESTS_INSTALL', 'doma', '${XT_DIR_ABS_ROOTFS_DOMA}', '', d)} \
"

python __anonymous () {
    # use domu as default
    udomains = (d.getVar('XT_GUESTS_INSTALL') or "domu ").split()
    d.setVar('DOM0_XT_UBOOT_PACKAGES',"")
    packages = {
       'domu': d.getVar('XT_DIR_ABS_ROOTFS_DOMU',True),
       'doma': d.getVar('XT_DIR_ABS_ROOTFS_DOMA',True),
       'domd': d.getVar('XT_DIR_ABS_ROOTFS_DOMD',True)
    }
    for dom in udomains:
       if dom not in packages.keys():
           raise bb.parse.SkipPackage('%s is not supported' % (dom))
       d.appendVar("DOM0_XT_UBOOT_PACKAGES", "%s\n" % packages[dom])
       d.appendVar("DOMAINS", "%s:%s " % (dom, packages[dom]))

    if "domd" not in udomains:
       d.appendVar("DOM0_XT_UBOOT_PACKAGES", "%s\n" % packages['domd'])
       d.appendVar("DOMAINS", "%s:%s " % ("domd", packages['domd']))
    
    overrides = d.getVar('MACHINEOVERRIDES').split(':')
    # here we define 'fake' name of defconfig, because at the moment of the
    # implementation it is not ready yet
    # it needs to be updated with valid one
    if "imx8qmolu-c1" in overrides:
       d.setVar('XT_DOMD_DEFCONFIG',"not-existed-domd_defconfig")
    else:
       raise bb.parse.SkipPackage('No supported machine in the list of overrides %s' % d.getVar('MACHINEOVERRIDES'))
}

do_install_append() {
+     for dom in ${DOMAINS}
+        do
+              path=$(echo ${dom} | sed 's/.*://')
+              key=$(echo ${dom} | sed 's/:.*//')
+     
+              install -d ${D}/${path}
+              install -m 0744 ${D}/boot/u-boot-${key}-${PV}-${PR}.${UBOOT_SUFFIX}  ${D}/${path}/u-boot.bin
+              rm -f ${D}/boot/u-boot-${key}-${PV}-${PR}.${UBOOT_SUFFIX}
+        done
}
