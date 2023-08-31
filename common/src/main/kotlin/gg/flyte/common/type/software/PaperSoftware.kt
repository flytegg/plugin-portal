package gg.flyte.common.type.software

import gg.flyte.common.type.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.software.`interface`.SoftwareInterface

object PaperSoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "paper" }