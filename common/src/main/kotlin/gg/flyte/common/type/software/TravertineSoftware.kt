package gg.flyte.common.type.software

import gg.flyte.common.type.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.software.`interface`.SoftwareInterface

object TravertineSoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "travertine" }