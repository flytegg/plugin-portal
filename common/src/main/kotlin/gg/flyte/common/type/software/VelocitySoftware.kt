package gg.flyte.common.type.software

import gg.flyte.common.type.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.software.`interface`.SoftwareInterface

object VelocitySoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "velocity" }